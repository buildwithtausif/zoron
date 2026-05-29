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
    dest = r"d:\Projects\zoron\releases\zoron_v4.5.0.zip"
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    zip_dir(source, dest)
