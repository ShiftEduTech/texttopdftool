from pathlib import Path
from PIL import Image

assets = Path(
    r"C:\Users\RAMESH POLAMARASETTI\.cursor\projects\c-Users-RAMESH-POLAMARASETTI-OneDrive-Documents-GitHub-texttopdftool\assets"
)
static = Path(
    r"c:\Users\RAMESH POLAMARASETTI\OneDrive\Documents\GitHub\texttopdftool\src\main\resources\static"
)
images = static / "images"
images.mkdir(parents=True, exist_ok=True)

app_icon = Image.open(assets / "codepdf-app-icon.png").convert("RGBA")
logo_mark = Image.open(assets / "codepdf-logo-mark.png").convert("RGBA")
og = Image.open(assets / "codepdf-og.png").convert("RGB")

logo_mark.save(images / "codepdf-logo.png", optimize=True)
logo_mark.save(images / "codepdf-logo-mark.png", optimize=True)

og.resize((1200, 630), Image.Resampling.LANCZOS).save(
    images / "og-image.png", optimize=True, quality=92
)


def fit_square(im: Image.Image, size: int) -> Image.Image:
    im = im.copy()
    im.thumbnail((size, size), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    x = (size - im.width) // 2
    y = (size - im.height) // 2
    canvas.paste(im, (x, y), im)
    return canvas


for size, name in [(192, "icon-192.png"), (512, "icon-512.png"), (180, "apple-touch-icon.png")]:
    fit_square(app_icon, size).save(static / name, optimize=True)

for size, name in [(16, "favicon-16.png"), (32, "favicon-32.png"), (48, "favicon-48.png")]:
    fit_square(logo_mark, size).save(static / name, optimize=True)

ico_images = [fit_square(logo_mark, s) for s in (16, 32, 48)]
ico_images[0].save(
    static / "favicon.ico",
    format="ICO",
    sizes=[(16, 16), (32, 32), (48, 48)],
    append_images=ico_images[1:],
)

# Remove old brand file if present
old = images / "shiftedutech-logo.png"
if old.exists():
    old.unlink()

print("Brand assets installed.")
