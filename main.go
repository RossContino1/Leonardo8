package main

import (
	"fmt"
	"os"
	"os/exec"

	"fyne.io/fyne/v2"
	"fyne.io/fyne/v2/app"
	"fyne.io/fyne/v2/container"
	"fyne.io/fyne/v2/dialog"
	"fyne.io/fyne/v2/widget"
)

func main() {
	a := app.New()
	w := a.NewWindow("FFmpeg Converter")

	var inputPath, outputPath string
	logLabel := widget.NewLabel("Select input and output files to begin.")

	// --- Input File Selection ---
	inputBtn := widget.NewButton("Select Input File", func() {
		dialog.NewFileOpen(func(r fyne.URIReadCloser, err error) {
			if err != nil {
				dialog.ShowError(err, w)
				return
			}
			if r == nil {
				return
			}
			inputPath = r.URI().Path()
			logLabel.SetText(fmt.Sprintf("Input selected: %s", inputPath))
		}, w).Show()
	})

	// --- Output File Selection ---
	outputBtn := widget.NewButton("Select Output File", func() {
		dialog.NewFileSave(func(wr fyne.URIWriteCloser, err error) {
			if err != nil {
				dialog.ShowError(err, w)
				return
			}
			if wr == nil {
				return
			}
			outputPath = wr.URI().Path()
			logLabel.SetText(fmt.Sprintf("Output file: %s", outputPath))
		}, w).Show()
	})

	// --- Convert Button ---
	convertBtn := widget.NewButton("Convert", func() {
		if inputPath == "" || outputPath == "" {
			dialog.ShowInformation("Missing File", "Please select both input and output files.", w)
			return
		}

		// Optional safety: check if output file already exists
		if _, err := os.Stat(outputPath); err == nil {
			confirm := dialog.NewConfirm("File Exists",
				fmt.Sprintf("The file '%s' already exists.\nDo you want to overwrite it?", outputPath),
				func(ok bool) {
					if ok {
						runFFmpeg(a, w, inputPath, outputPath, logLabel)
					} else {
						logLabel.SetText("Conversion canceled.")
					}
				}, w)
			confirm.Show()
			return
		}

		// Run ffmpeg directly if file doesn't exist
		runFFmpeg(a, w, inputPath, outputPath, logLabel)
	})

	// --- Layout ---
	w.SetContent(container.NewVBox(
		widget.NewLabel("🎞️ Leonardo v8 (Go Based)"),
		inputBtn,
		outputBtn,
		convertBtn,
		logLabel,
	))

	w.Resize(fyne.NewSize(420, 250))
	w.ShowAndRun()
}

// --- Helper function to run ffmpeg safely ---
func runFFmpeg(a fyne.App, w fyne.Window, inputPath, outputPath string, logLabel *widget.Label) {
	logLabel.SetText("Running ffmpeg... please wait.")

	go func() {
		// Use -y to overwrite existing files automatically
		cmd := exec.Command("ffmpeg",
			"-y", // <— overwrite existing files without prompt
			"-i", inputPath,
			"-vcodec", "mjpeg",
			"-q:v", "2",
			"-acodec", "pcm_s16be",
			"-q:a", "0",
			"-f", "mov",
			outputPath,
		)

		cmd.Stdout = os.Stdout
		cmd.Stderr = os.Stderr

		err := cmd.Run()
		if err != nil {
			a.SendNotification(&fyne.Notification{
				Title:   "FFmpeg Error",
				Content: err.Error(),
			})
			logLabel.SetText("Error: " + err.Error())
			return
		}

		a.SendNotification(&fyne.Notification{
			Title:   "Conversion Complete",
			Content: fmt.Sprintf("Saved to %s", outputPath),
		})
		logLabel.SetText("✅ Conversion complete!")
	}()
}
