import sys
import os

def full_to_half_char(char):
    """
    将单个全角字符转换为半角字符
    """
    code = ord(char)
    
    # 1. 处理全角空格 (U+3000 -> U+0020)
    if code == 0x3000:
        return ' '
    
    # 2. 处理全角数字、字母、符号 (U+FF01 ~ U+FF5E)
    # 范围对应 ASCII 的 ! 到 ~
    if ord("０") <= code <= ord("～"):
        return chr(code - 0xFEE0)
    
    # 3. 其他字符（包括中文汉字、全角中文标点等）保持不变
    return char

def convert_file_manual(input_path, output_path=None):
    if not os.path.exists(input_path):
        print(f"错误: 文件 '{input_path}' 不存在。")
        return

    if output_path is None:
        base, ext = os.path.splitext(input_path)
        output_path = f"{base}_half_manual{ext}"

    try:
        with open(input_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # 逐字符转换
        converted_chars = [full_to_half_char(c) for c in content]
        converted_content = ''.join(converted_chars)

        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(converted_content)
            
        print(f"手动转换成功！输出文件: {output_path}")

    except Exception as e:
        print(f"发生错误: {e}")

if __name__ == '__main__':
    filename = "test.txt"  # <--- 请在此处修改你的文件名
    if len(sys.argv) > 1:
        filename = sys.argv[1]
    convert_file_manual(filename)
