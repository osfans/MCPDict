import zipfile
import glob
for input_zip_file in glob.glob("*.zip"):
	output_tsv_file = input_zip_file.replace('.zip', '.tsv')
	print(f"Processing {input_zip_file} -> {output_tsv_file}")
	with zipfile.ZipFile(input_zip_file, 'r') as zip_ref:
		with open(output_tsv_file, 'w', encoding='utf-8') as tsv_file:
			for file_name in zip_ref.namelist():
				if not (file_name.endswith('.txt') or file_name.endswith(".hsdp")):  # Only process .txt or .hsdp files in the zip
					continue
				with zip_ref.open(file_name) as f:
					for line in f:
						tsv_file.write(line.decode('utf-8').rstrip() + '\n')
