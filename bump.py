import os

files = [
    r'd:\Projects\zoron\build_zip.py',
    r'd:\Projects\zoron\ota.json',
    r'd:\Projects\zoron\update.json',
    r'd:\Projects\zoron\src\WhyredController\app\build.gradle',
    r'd:\Projects\zoron\src\whyred_module\module.prop'
]

for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    content = content.replace('v4.5.4', 'v4.5.5')
    content = content.replace('versionCode 38', 'versionCode 39')
    content = content.replace('"versionCode": 38', '"versionCode": 39')
    content = content.replace('versionCode=38', 'versionCode=39')
    with open(f, 'w', encoding='utf-8') as file:
        file.write(content)
print('Updated versions')
