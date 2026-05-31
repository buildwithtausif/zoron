import os
import zipfile

def zip_dir(source_dir, zip_filepath):
    with zipfile.ZipFile(zip_filepath, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, _, files in os.walk(source_dir):
            for file in files:
                file_path = os.path.join(root, file)
                # Ensure forward slashes for internal zip paths
                arcname = os.path.relpath(file_path, source_dir).replace('\\', '/')
                zipf.write(file_path, arcname)
    print(f"Created {zip_filepath} successfully.")

if __name__ == "__main__":
    source = r"d:\Projects\zoron\src\whyred_module"
    # Read version dynamically from module.prop
    version = "v0.0.0"
    prop_path = os.path.join(source, "module.prop")
    if os.path.exists(prop_path):
        with open(prop_path, "r") as f:
            for line in f:
                if line.startswith("version="):
                    version = line.strip().split("=", 1)[1]
                    break
    dest = rf"d:\Projects\zoron\releases\zoron_{version}.zip"
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    zip_dir(source, dest)

