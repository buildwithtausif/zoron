from PIL import Image
import os

img_path = r"C:\Users\conne\.gemini\antigravity\brain\0642518f-4199-40b1-a34a-df4277a277ba\zoron_logo_1779365849887.png"
out_dir = r"d:\Projects\zoron\WhyredController\app\src\main\res"

sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

img = Image.open(img_path)

for folder, size in sizes.items():
    folder_path = os.path.join(out_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(os.path.join(folder_path, "ic_launcher.png"))
    resized.save(os.path.join(folder_path, "ic_launcher_round.png"))

print("Icons generated!")
