# 🎬 Leonardo — FFmpeg Video Converter for DaVinci Resolve (Linux)

[![Download](https://img.shields.io/badge/Download-Leonardo-blue?style=for-the-badge)](https://bytesbreadbbq.com/track_download.php?file=2025/11/leonardo.zip)

Leonardo is a simple, fast desktop utility for converting video formats into files that work smoothly inside **DaVinci Resolve on Linux**.  
It provides an easy graphical interface to **FFmpeg**, so you can prepare footage without memorizing command-line syntax.

---
## 🎥 Video Demonstrations

- **Full Demonstration:** [Watch on YouTube](https://youtu.be/EcrWtsoLaY8)
- **59-Second Overview:** [Watch the Short](https://www.youtube.com/shorts/SvsJmhlpx9Q?feature=share)


## 📥 Download Precompiled Binary

You can download the latest precompiled version of Leonardo here:

[Download Leonardo](https://bytesbreadbbq.com/track_download.php?file=2025/11/leonardo.zip)

After downloading:

1. Unzip the file:
```bash
unzip leonardo.zip

    Make it executable:

chmod +x leonardo

    (Optional) Move it to a system path:

sudo mv leonardo /usr/local/bin/

Now you can run it anywhere:

leonardo

🧩 Why Use Leonardo with DaVinci Resolve

DaVinci Resolve on Linux can be picky about codecs and containers.
Many cameras record in formats that Resolve won’t import or play correctly.

Leonardo automatically converts your footage into a .MOV container using MJPEG (video) and PCM (audio) — both Resolve-friendly formats that maintain high quality while ensuring compatibility.
⚙️ Requirements
Component	Purpose	Notes
Linux OS	App tested on Garuda, Ubuntu, Fedora	Works with both X11 and Wayland
FFmpeg	Video conversion backend	Must be installed and in your PATH
Go runtime (optional)	Only needed if building from source	Precompiled binaries don’t require it
Fyne GUI toolkit	Used internally	Compiled into Leonardo — no external install needed
🛠️ Installing FFmpeg (Required)

Leonardo calls FFmpeg behind the scenes.
If FFmpeg isn’t installed, Leonardo cannot perform conversions.
Install FFmpeg via your package manager:

# Debian / Ubuntu
sudo apt install ffmpeg

# Fedora
sudo dnf install ffmpeg

# Arch / Garuda
sudo pacman -S ffmpeg

To confirm installation:

ffmpeg -version

🖥️ How to Use Leonardo

    Launch Leonardo

    ./leonardo

    or open it from your Applications menu.

    Select Input File
    Choose your original video (e.g., .mp4, .mov, .avi, .mkv).

    Select Output File
    Choose where to save the new file (e.g., converted.mov).

    Click “Convert”
    Leonardo runs FFmpeg and will display a completion message when finished.

    Import into DaVinci Resolve
    The resulting .mov file should import and play smoothly within Resolve’s Media Pool.

⚠️ Important Notes & Common Pitfalls
1. Spaces in File or Folder Names

FFmpeg often misbehaves when file paths contain spaces — especially on Linux.
Leonardo automatically attempts to quote paths correctly, but some builds of FFmpeg still reject spaces.

Best practice:
✅ Rename your files and folders using underscores or hyphens before conversion.
Example:

❌ /home/ross/Videos/My Cool Clip.mov  
✅ /home/ross/Videos/My_Cool_Clip.mov

2. Overwrite Warning

If the output file already exists, FFmpeg may refuse to overwrite it unless forced.
Leonardo automatically includes the overwrite flag -y, but some distros prompt anyway.
Delete or rename existing output files before reconverting.
3. Resolve Codec Compatibility

DaVinci Resolve (especially on Linux) supports only certain codecs:

    ✅ MJPEG + PCM (MOV) — supported and ideal for editing

    ⚠️ H.265 / HEVC — often needs paid license or NVIDIA GPU acceleration

    ⚠️ VP9, AV1 — not supported natively

Leonardo uses MJPEG video + PCM audio for maximum compatibility.
🎨 Example FFmpeg Command (Used Internally)

Leonardo runs the equivalent of:

ffmpeg -i input.mp4 -vcodec mjpeg -q:v 2 -acodec pcm_s16be -q:a 0 -f mov output.mov

This produces a high-quality, uncompressed .mov file that Resolve reads easily.
🧠 Troubleshooting
Issue	Possible Fix
FFmpeg not found	Ensure it’s installed and in your PATH
No GUI appears	Check you’re running under X11 or Wayland
“File already exists”	Delete or rename the target .mov file
Resolve still can’t import	Try re-exporting using a shorter path or no spaces
🛠️ Build from Source (Optional)

Developers can rebuild Leonardo easily:

go get fyne.io/fyne/v2
go build -o leonardo main.go

To cross-compile (for example, for Windows):

GOOS=windows GOARCH=amd64 go build -o leonardo.exe main.go

📄 License

Leonardo is free for personal and educational use.
You may modify or redistribute it freely with attribution.
💬 Credits

    Developed with: Go + Fyne

    Powered by: FFmpeg

    Optimized for: DaVinci Resolve users on Linux

    “Leonardo paints video conversions with precision and simplicity.” 🎨
