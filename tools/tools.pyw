from tkinter import *
from tkinter import ttk, filedialog, font, scrolledtext
import tkinter as tk
import os, sys, ctypes, shutil
from pathlib import Path
import subprocess

VERSION = 20260207
os.environ['PYTHONIOENCODING'] = 'utf-8'
WORKSPACE = os.path.dirname(os.path.abspath(__file__))
os.chdir(WORKSPACE)
output_file = ""

try:
    import msvcrt
except ModuleNotFoundError:
    is_windows = False
else:
    is_windows = True

def enable_windows_hdpi():
    """启用Windows HDPI支持"""
    try:
        # Windows 8.1及以上版本
        ctypes.windll.shcore.SetProcessDpiAwareness(1)
        # print("已启用Windows HDPI支持")
    except:
        try:
            # Windows Vista及以上版本
            ctypes.windll.user32.SetProcessDPIAware()
        except:
            print("无法启用HDPI支持")

# 在创建主窗口前调用
if is_windows:
    # Create a STARTUPINFO object
    startupinfo = subprocess.STARTUPINFO()

    # Set the dwFlags and wShowWindow attributes to hide the window
    # STARTF_USESHOWWINDOW tells Windows to use the wShowWindow flag
    startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
    # SW_HIDE tells Windows to hide the window
    startupinfo.wShowWindow = subprocess.SW_HIDE
    enable_windows_hdpi()
else:
    startupinfo = None

import importlib.util
def is_installed(package_name):
    spec = importlib.util.find_spec(package_name)
    return spec is not None

def install_pip():
    if not (is_installed("openpyxl") and is_installed("docx")):
        os.system(f"{sys.executable} -m pip install -r requirements.txt -i https://mirrors.tuna.tsinghua.edu.cn/pypi/web/simple")
    if is_windows and not is_installed("win32com"):
        os.system(f"{sys.executable} -m pip install pywin32 -i https://mirrors.tuna.tsinghua.edu.cn/pypi/web/simple")

install_pip()

def select_file():
    file_path = filedialog.askopenfilename(
        title="選擇字表文件",
        filetypes=[("所有文件", "*.docx;*.xlsx;*.txt;*.tsv;*.csv;*.md".split(";")),("Word文檔", "*.docx"),("Excel表格", "*.xlsx"),("文本文件", "*.txt;*.tsv;*.csv;*.md".split(";"))]
    )
    if file_path:
        labelInfo["text"] = file_path
        # 在这里处理文件
        spath = os.path.join(WORKSPACE, os.path.basename(file_path))
        try:
            shutil.copyfile(file_path, spath)
        except:
            # samefile
            pass
        path = Path(spath)
        path.touch(exist_ok=True)
        try:
            if os.path.exists("warnings.txt"):
                os.remove("warnings.txt")
            output_file = spath + ".tsv"
            # Run a command and capture its output
            result = subprocess.run(
                [sys.executable, os.path.join(WORKSPACE, "make.py"), spath, "-o", output_file],           # Command and arguments as a list
                cwd=WORKSPACE,
                startupinfo=startupinfo,
                capture_output=True,    # Capture stdout and stderr
                text=True,              # Return output as string (Python 3.7+)
                encoding='utf-8',
                check=False              # Raise an exception if the command fails
            )
            output = result.stdout + result.stderr
            text_widget.delete('1.0', tk.END)
            # Insert the output into the ScrolledText widget at the 'end'
            text_widget.insert(tk.END, output)
            if os.path.exists("warnings.txt"):
                warnings = open("warnings.txt", encoding="U16").read()
                text_widget.insert(tk.END, warnings)
            # Automatically scroll to the end to show the latest output
            text_widget.see(tk.END)
        except:
            pass

class App(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title(f"漢字音典字表工具 {VERSION}")
        #self.maxsize(1000, 400)
        font.nametofont("TkDefaultFont")["size"]=12
        document = "漢字音典字表檔案（長期更新）.xlsx"
        if not os.path.exists(document):
            ttk.Label(self, text=f"如需修改檔案，請將“{document}”放在當前目錄：{WORKSPACE}").pack()
        ttk.Button(self, text="選擇字表", command=select_file).pack()

# create the application
root = App()

labelInfo = ttk.Label(root)
labelInfo.pack()
text_widget = scrolledtext.ScrolledText(root)
text_widget.pack(fill=BOTH, expand=True, padx=2, pady=2)

root.mainloop()