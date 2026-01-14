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
	w := a.NewWindow("Leonardo v9")

	var inputPath, outputPath string
	conversionMode := "mp4_to_mov" // default

	logLabel := widget.NewLabel("Select input, output, and conversion type.")

	// --- Conversion Mode ---
	modeSelector := widget.NewRadioGroup(
		[]string{
			"Convert MP4 → MOV (DaVinci Resolve)",
			"Convert MOV → MP4",
		},
		func(value string) {
			if value == "Convert MOV → MP4" {
				conversionMode = "mov_to_mp4"
			} else {
				conversionMode = "mp4_to_mov"
			}
		},
	)
	modeSelector.SetSelected("Convert MP4 → MOV (DaVinci Resolve)")

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

		if _, err := os.Stat(outputPath); err == nil {
			confirm := dialog.NewConfirm("File Exists",
				fmt.Sprintf("The file '%s' already exists.\nOverwrite?", outputPath),
				func(ok bool) {
					if ok {
						runFFmpeg(a, w, inputPath, outputPath, conversionMode, logLabel)
					} else {
						logLabel.SetText("Conversion canceled.")
					}
				}, w)
			confirm.Show()
			return
		}

		runFFmpeg(a, w, inputPath, outputPath, conversionMode, logLabel)
	})

	w.SetContent(container.NewVBox(
		widget.NewLabel("Leonardo v9 (Go Based)"),
		modeSelector,
		inputBtn,
		outputBtn,
		convertBtn,
		logLabel,
	))

	w.Resize(fyne.NewSize(450, 320))
	w.ShowAndRun()
}

// ----------------------------------------
// runFFmpeg chooses the command dynamically
// ----------------------------------------
func runFFmpeg(a fyne.App, w fyne.Window, inputPath, outputPath, mode string, logLabel *widget.Label) {
	logLabel.SetText("Running ffmpeg... please wait.")

	go func() {
		var cmd *exec.Cmd

		if mode == "mp4_to_mov" {
			cmd = exec.Command("ffmpeg",
				"-y",
				"-i", inputPath,
				"-vcodec", "mjpeg",
				"-q:v", "2",
				"-acodec", "pcm_s16be",
				"-q:a", "0",
				"-f", "mov",
				outputPath,
			)
		} else {
			// MOV → MP4
			cmd = exec.Command("ffmpeg",
				"-y",
				"-i", inputPath,
				outputPath,
			)
		}

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
		logLabel.SetText("Conversion complete.")
	}()
}
