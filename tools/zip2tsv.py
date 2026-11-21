import zipfile
import glob
import os.path
for input_zip_file in glob.glob("*.zip"):
	output_tsv_file = input_zip_file.replace('.zip', '.tsv')
	print(f"Processing {input_zip_file} -> {output_tsv_file}")
	with zipfile.ZipFile(input_zip_file, 'r') as zip_ref:
		with open(output_tsv_file, 'w', encoding='utf-8') as tsv_file:
			for file_name in sorted(zip_ref.namelist(),key=lambda x: (os.path.splitext(x)[1], x)):  # Sort by directory depth and then alphabetically
				ext = os.path.splitext(file_name)[1]
				if ext not in ('.ini', '.txt', '.hsdp'):  # Only process .txt or .hsdp files in the zip
					continue
				with zip_ref.open(file_name) as f:
					for line in f:
						tsv_file.write(line.decode('utf-8').rstrip() + '\n')
