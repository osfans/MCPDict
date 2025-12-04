import sqlite3, os, re, sys, argparse
from pypinyin import pinyin, Style
from opencc import OpenCC

t2s = OpenCC('t2s')
def 普拼(word):
	return pinyin(t2s.convert(re.sub("[《》（）]", "", word)), style=Style.TONE3, heteronym=False)

parser = argparse.ArgumentParser()
parser.add_argument('-t', '--test', action='store_true', help='測試模式', required=False)
args, argv = parser.parse_known_args()

IPA_PATTERN = re.compile("^([ʔʡˀʕʢbɓɸβʙpmɰɱfʩɟdɗɖᶑʣʤʥꭦðtʈʦʧʨꭧθnŋɲɳȵɴlɬɭʟ𝼄ɮ𝼅ʪʫgɡɠɢʛkʞhħɦɧʰʜzʐʑʒʓcʗçɕsʂrɹɻɺ𝼈ɾɽʀʁʃʄʆjʝʲqxχvʋⱱɣwʍʷʎ𝼆ʼ'ʘǀǃǁǂ𝼊\u0300-\u0362]*)([^\\d]+)?([\\d]+[a-z]?)?$")

#30/60/10
WORDS = ['丙炳秉柄餅', '病被', '甫', '浦普', '斧府腑', '虎呼', '黃黄皇煌凰', '王', '同童', '洞動', '研硯', '年念', '良梁粱', '娘孃釀', '若弱箬', '落洛駱', '角覺豇', '脚腳姜薑', '去', '口開', '環', '還華淮懷', '鹹咸', '喊', '雞計繼', '資次恣姊', '雞計繼', '低底', '九玖久韭灸', '酒尖井', '詳翔像', '牆墻薔', '乞迄契起欺', '喫吃', '訓熏薰燻勳', '順吮', '照招昭沼釗', '趙兆', '早澡', '找笊渣榨爭争', '詩試始書舒', '師獅', '純醇淳鶉', '順吮', '書舒', '虛虚', '扇煽羶', '線綫', '然燃', '言', '文聞蚊雯紋', '完丸', '眼', '安案按晏', '我', '鵝娥俄餓', '容溶蓉鎔', '用勇踊蛹俑浴', '姐借', '子仔兹滋', '井精青清', '進津盡儘尽晋晉', '朋鵬', '盆笨噴本奔', '噸頓敦墩屯鈍', '燈登凳', '孫損存尊', '森', '困睏坤', '孔空控', '人仁', '然燃', '令零鈴玲領拎', '憐蓮練', '刪删', '拴閂', '染冉', '軟', '賴奈', '懶蘭攔爛', '梅每莓煤媒妹', '棉綿緜', '多馱拖舵', '端團团', '關慣摜', '光廣王黃黄皇煌凰', '關慣摜', '官管館冠灌觀', '汗寒旱捍悍翰韓', '換喚完歡寬煥', '奸姦', '姜薑', '奸姦澗諫', '肩堅賢', '戰', '站', '姐借', '賈價假', '野夜耶椰', '以已異', '社射車舍扯撦蛇', '曬', '車舍扯撦蛇社射', '哥歌可珂河何', '內内雷', '貝狽䟺鋇沛', '最', '肥費非飛妃', '車舍扯撦蛇社射', '費肥飛非妃', '飛非妃肥', '梯體涕替', '瘸靴', '茄斜', '靴瘸', '耍抓', '靴瘸', '雖醉翠', '圍韋偉葦胃謂渭蝟', '圓員援媛', '雷內内', '驢旅呂吕侶侣慮', '雨羽宇禹', '五伍', '書恕處', '梳初', '湖胡瑚猢狐孤姑古', '河何哥歌可珂', '哥歌可珂河何', '鍋果過', '哥哥歌可珂何河', '溝構勾購夠够彀', '個箇个', '夠够彀溝構購', '貿茂', '墓募慕暮', '母畝亩拇牡', '某', '某', '媒煤妹梅每莓', '米謎迷', '美眉', '耳二而', '馬碼罵駡', '二耳而', '奧懊襖袄澳', '也', '矮隘', '甲胛鉀', '脚腳藥葯約', '八捌拔', '剝剥駁雹', '莫漠摸幕寞', '末沫茉', '落洛駱', '六陸', '木沐鹿讀目', '麥脉脈', '木沐鹿讀目', '默墨', '百柏', '北', '粥熟軸', '竹竺筑', '竹竺筑', '骨忽', '縮', '說説悅悦閲閱', '盒合', '活闊', '喝渴葛褐', '黑肋勒得', '赤尺石', '色嗇', '則測側', '哲折徹澈', '日', '熱', '鐵跌', '踢剔惕滴迪笛狄', '蟹', '海開', '花瓜天', '話會', '販半報變放', '飯壞', '救半報變放', '舅', '劇', '據鋸去', '瘦湊臭夠够彀', '肉', '測側', '廁', '式拭軾𢂑飾', '試瘦湊臭夠够彀', '失溼濕', '釋適', '溼濕失', '石']
PART_METHODS = ['地圖集二分區', '音典分區']
WORKSPACE = os.path.dirname(os.path.abspath(__file__))
os.chdir(WORKSPACE)

def get_mcpdict():
	if os.path.exists('mcpdict.db'):
		NAME = 'mcpdict.db'
	else:
		NAME = os.path.join('../..', 'app/src/main/assets/databases/mcpdict.db')
	conn = sqlite3.connect(NAME)
	c = conn.cursor()
	where = " OR ".join(argv)
	if where: where = "where 語言 MATCH '" + where + "'"
	c.execute(f"select * from langs {where};")
	dicts = dict()
	for item in c.fetchall():
		word = item[0].split(" ")
		for lang in word:
			for hz in set(WORDS):
				if lang in hz:
					d = dicts.setdefault(item[1], dict())
					d.setdefault(lang, set()).add((item[2], item[3]))
	infos = dict()
	c.execute(f"select 簡稱,{','.join(PART_METHODS)} from info where 音節數 is NOT NULL;")
	for item in c.fetchall():
		part = item[1:]
		infos[item[0]] = part
	conn.close()
	return dicts, infos

def split_ipa(ipa):
	l = ipa.strip("`*\\?\\+")
	l = re.sub("\\(.*?\\)", "", l)
	if not l:
		return None
	m = IPA_PATTERN.findall(l)
	if not m:
		return None
	p = list(m[0])
	if p[0] and not p[1] and p[0][-1] in ("\u030d", "\u0329"):
		p[1] = p[0][-2:]
		if len(p[0]) == 2:
			p[0] = p[0][:-1]
		elif len(p[0]) > 2:
			p[0] = p[0][:-2]
	return tuple(p)

def get_part(lang, item, hzs, index=0):
	ret = set()
	yd = index // 3
	index = index % 3
	for hz in hzs:
		if hz in item:
			n = len(item[hz])
			for ipa, info in item[hz]:
				info = re.sub("\\*.\\*", "~", info)
				info = t2s.convert(info.replace(" ", "").replace("*", ""))
				if info.startswith("训") or info.startswith("(训"):
					continue
				if n > 1:
					if yd == 0 and (info.startswith("文") or info.startswith("(文")):
						continue
					if yd == 1 and (info.startswith("白") or info.startswith("(白")):
						continue
					if info in ("又", "又讀") or info.startswith("(又"):
						continue
					if info.startswith("存疑") or info.startswith("(存疑)"):
						continue
				ipas = ipa.split("/")
				if lang in ("中原音韻",):
					ipas = ipas[0:1]
				if lang in ("廣韻",):
					ipas[0] = re.sub("q$", "3", ipas[0])
					ipas[0] = re.sub("h$", "5", ipas[0])
					ipas[0] = re.sub("([ptk])$", "\\g<1>7", ipas[0])
					ipas[0] = re.sub("([^357])$", "\\g<1>1", ipas[0])
					ipaindex = 9
					ipas[ipaindex] = ipas[ipaindex] + ipas[0][-1]
					ipas = ipas[ipaindex:ipaindex+1]
				if lang in ("白－沙上古",):
					continue
				for ipa in ipas:
					p = split_ipa(ipa)
					if not p: continue
					ret.add(p[index])
			break
	if args.test and len(ret) > 1:
		print(hz, ret)
	return ret

def get_result(start, end, lang, item, index=0):
	result = []
	for sm in range(start, end, 2):
		left, right = WORDS[sm:sm+2]
		left_set = get_part(lang, item, left, index)
		right_set = get_part(lang, item, right, index)
		b = '-'
		if left_set and right_set:
			b = str(1 - int(left_set.isdisjoint(right_set)))
		result.append(b)
	return result

def get_tsv():
	dialects = []
	for line in open("data.tsv", "r", encoding="U8"):
		parts = line.strip().split("\t")
		if parts:
			dialects.append(tuple(parts))
	return dialects

def dump_html(answers):
	import datetime
	now = datetime.datetime.now(tz=datetime.timezone(datetime.timedelta(hours=8)))
	results = []
	for items in answers:
		result = [f"new Array{tuple(item)}" for item in items]
		results.append("new Array(%s)" % ",\n".join(result))
	answer = ",\n".join(results)
	# print(answer)
	html = open("template.html", "r", encoding="U8").read().replace("{{DIALECTS}}", answer).replace("{{DATE}}", " (" + now.strftime("%Y-%m-%d") + ")")
	f = open("sim.html", "w", encoding="U8")
	f.write(html)
	f.close()

def find_nearest(value, dialects, method="", max=2):
	min_diff = 100
	nearest = None
	scores = dict()
	for lang, m, ipa in dialects:
		if method and m != method:
			continue
		score = []
		for a, b in zip(value, ipa):
			if a == '-' or b == '-':
				continue
			score.append(a == b)
		scores[lang] = score.count(True) * 100 / len(score) if score else 0
	return dict(sorted(scores.items(), key=lambda x: x[1], reverse=True)[:max])

def main():
	dialects = get_tsv()
	dicts, infos = get_mcpdict()
	for lang, item in dicts.items():
		score = []
		score.extend(get_result(0, 30 * 2, lang, item, index=0))
		score.extend(get_result(30 * 2, 90 * 2, lang, item, index=1))
		score.extend(get_result(90 * 2, 100 * 2, lang, item, index=2))
		score.extend(get_result(0, 30 * 2, lang, item, index=3))
		score.extend(get_result(30 * 2, 90 * 2, lang, item, index=4))
		score.extend(get_result(90 * 2, 100 * 2, lang, item, index=5))
		白讀 = "".join(score[0:100])
		文讀 = "".join(score[100:200])
		if 白讀.count('-') > 20:
			if 文讀.count('-') <= 20: dialects.append((lang, "音典", 文讀))
			continue
		elif 文讀.count('-') > 20:
			dialects.append((lang + "", "音典", 白讀))
			continue
		if 白讀 != 文讀:
			dialects.append((lang + "(白讀)", "音典", 白讀))
			dialects.append((lang + "(文讀)", "音典", 文讀))
		else:
			dialects.append((lang +"", "音典", 白讀))
	
	parts = dict()
	for _name, author, value in dialects:
		name = _name.split("(")[0]
		if author != "音典" or name not in infos:
			continue
		for method in PART_METHODS:
			part = infos[name][PART_METHODS.index(method)]
			if not part: continue
			parts.setdefault(method, dict()).setdefault(part, list()).append(value)
	part_results = dict()
	for method, items in parts.items():
		method_results = dict()
		for part, values in items.items():
			results = []
			for i in range(100):
				bs = [v[i] for v in values]
				b = '-'
				valid = len(bs) - bs.count('-')
				if valid == 0:
					results.append(b)
					continue
				score = bs.count('1') * 100 / valid
				delta = 30
				if score >= 50 + delta:
					b = '1'
				elif score <= 50 - delta:
					b = '0'
				results.append(b)
			method_results.setdefault(part, list()).append("".join(results))
		for part, values in method_results.items():
			results = []
			for i in range(100):
				bs = [v[i] for v in values]
				b = '-'
				valid = len(bs) - bs.count('-')
				if valid == 0:
					results.append(b)
					continue
				score = bs.count('1') * 100 / valid
				delta = 30
				if score >= 50 + delta:
					b = '1'
				elif score <= 50 - delta:
					b = '0'
				results.append(b)
			method_results[part] = "".join(results)
		skips = ("官話", "戲劇", "歷史音", "現代標準漢語")
		for skip in skips:
			method_results.pop(skip, None)
		part_results[method] = method_results
	part_answers = []
	answers = []
	dialects.sort(key=lambda x: 普拼(x[0]))
	answers.append(dialects)
	for method, results in part_results.items():
		results = [(k, method, v) for k, v in results.items()]
		results.sort(key=lambda x: 普拼(x[0]))
		answers.append(results)
	dump_html(answers)
	if args.test:
		count, wrong = 0, 0
		method = PART_METHODS[1]
		for _name, author, value in dialects:
			name = _name.split("(")[0]
			if author != "音典" or name not in infos:
				continue
			count += 1
			part = infos[name][PART_METHODS.index(method)]
			results = find_nearest(value, part_answers, method=method, max=1)
			if part not in results and part not in skips:
				wrong += 1
				# print(f"{_name} 實際分區 {part}，最接近分區 {results}")
		print(f"分區比對錯誤率：{wrong * 100 / count:.2f}% ({wrong}/{count})")

if __name__ == "__main__":
	main()