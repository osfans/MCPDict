(() => {
  'use strict';

  function getBridge() {
    if (!window.YindianBridge) {
      throw new Error('未找到音典 Android 本地數據接口');
    }
    return window.YindianBridge;
  }

  function getBridgeMethod(name) {
    const bridge = getBridge();
    const method = bridge?.[name];
    if (typeof method !== 'function') {
      throw new Error(`Android 本地接口缺少 ${name}；請確認 release 混淆規則已保留 ToolsBridge，並重新安裝新 APK`);
    }
    return method.bind(bridge);
  }

  function callBridge(name, ...args) {
    return getBridgeMethod(name)(...args);
  }

  function parseBridgeJson(raw, label) {
    try {
      const value = JSON.parse(String(raw || '[]'));
      if (!Array.isArray(value)) throw new Error('返回值不是數組');
      return value;
    } catch (error) {
      throw new Error(`${label}數據解析失敗：${error.message}`);
    }
  }


  const STATUS_LABELS = {
    same: '全同',
    partial: '部分相同',
    different: '全异',
    missing: '缺资料',
  };

  const MODERN_MODE_LABELS = {
    full: '完整读音',
    initial: '声母',
    final: '韵母',
    tone: '声调',
    notone: '忽略声调',
  };

  const MIDDLE_DIMENSION_LABELS = {
    initial: '切韻聲母',
    voicing: '清濁',
    rime: '切韻韻母',
    division: '等',
    openness: '開合',
    she: '攝',
    tone: '切韻聲調',
    position: '完整音韻地位',
  };

  const MULTI_FILTER_IDS = new Set(['filterVoicing', 'filterDivision', 'filterOpenness', 'filterShe', 'filterTone', 'filterInitial', 'filterRime']);
  const ALL_FILTER_VALUE = '__ALL__';

  const SHE_ORDER = ['果', '假', '遇', '蟹', '止', '效', '流', '咸', '深', '山', '臻', '宕', '江', '曾', '梗', '通'];

  const INITIAL_GROUPS = [
    { title: '幫組', values: ['幫', '滂', '並', '明'] },
    { title: '端組', values: ['端', '透', '定', '泥', '來'] },
    { title: '精組', values: ['精', '清', '從', '心', '邪'] },
    { title: '知組', values: ['知', '徹', '澄', '娘'] },
    { title: '莊組', values: ['莊', '初', '崇', '生', '俟'] },
    { title: '章組', values: ['章', '昌', '船', '書', '常'] },
    { title: '日母', values: ['日'] },
    { title: '見組', values: ['見', '溪', '群', '疑'] },
    { title: '曉組', values: ['曉', '匣'] },
    { title: '影組', values: ['影', '云', '以'] },
  ];

  const RIME_GROUPS = [
    { title: '果攝', values: ['歌', '戈'] },
    { title: '假攝', values: ['麻'] },
    { title: '遇攝', values: ['模', '魚', '虞'] },
    { title: '蟹攝', values: ['咍', '灰', '泰', '皆', '佳', '夬', '祭', '廢', '齊'] },
    { title: '止攝', values: ['支', '脂', '之', '微'] },
    { title: '效攝', values: ['豪', '肴', '宵', '蕭'] },
    { title: '流攝', values: ['侯', '尤', '幽'] },
    { title: '咸攝', values: ['覃', '談', '咸', '銜', '鹽', '嚴', '添', '凡'] },
    { title: '深攝', values: ['侵'] },
    { title: '山攝', values: ['寒', '桓', '山', '刪', '仙', '元', '先'] },
    { title: '臻攝', values: ['痕', '魂', '眞', '臻', '諄', '殷', '文'] },
    { title: '宕攝', values: ['唐', '陽'] },
    { title: '江攝', values: ['江'] },
    { title: '曾攝', values: ['登', '蒸'] },
    { title: '梗攝', values: ['庚', '耕', '清', '青'] },
    { title: '通攝', values: ['東', '冬', '鍾'] },
  ];

  const INITIAL_CANONICAL = new Map([
    ['帮', '幫'], ['幫', '幫'], ['庄', '莊'], ['莊', '莊'], ['羣', '群'], ['群', '群'], ['孃', '娘'], ['娘', '娘'],
  ]);
  const RIME_CANONICAL = new Map([
    ['鱼', '魚'], ['魚', '魚'], ['废', '廢'], ['廢', '廢'], ['齐', '齊'], ['齊', '齊'], ['萧', '蕭'], ['蕭', '蕭'],
    ['谈', '談'], ['談', '談'], ['衔', '銜'], ['銜', '銜'], ['盐', '鹽'], ['鹽', '鹽'], ['严', '嚴'], ['嚴', '嚴'],
    ['删', '刪'], ['刪', '刪'], ['真', '眞'], ['眞', '眞'], ['谆', '諄'], ['諄', '諄'], ['阳', '陽'], ['陽', '陽'],
    ['东', '東'], ['東', '東'], ['钟', '鍾'], ['鐘', '鍾'], ['鍾', '鍾'],
  ]);

  const VOWELS = new Set([
    ...'aeiouyAEIOUY',
    ...'ɑɒæɛɜɞəɐɘɵɤɔœøɶɨʉɯɪʏʊɚɝɿʅᴀᴇ',
  ]);
  const GLIDES = new Set(['j', 'w', 'ɥ']);
  const SYLLABIC_MARKS = new Set(['̩', '̍']);

  const INITIAL_VOICING = buildInitialVoicingMap();

  const state = {
    dialects: [],
    dialectByShort: new Map(),
    guangyunEntries: null,
    middleRows: null,
    evolutionGroups: [],
    evolutionMatchedChars: new Set(),
    evolutionPositionCount: 0,
    evolutionLastModernRows: null,
    compareChars: [],
    compareRows: [],
    compareRawRows: [],
  };

  const $ = (id) => document.getElementById(id);
  const escapeHtml = (value) => String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');

  document.addEventListener('DOMContentLoaded', init);

  async function init() {
    bindUi();

    const [dialectResult, qysResult] = await Promise.allSettled([
      loadDialects(),
      loadParsedQysRimes(),
    ]);

    if (dialectResult.status !== 'fulfilled') {
      console.error(dialectResult.reason);
      showInlineStatus('evolutionProgress', `语言列表加载失败：${dialectResult.reason?.message || dialectResult.reason}`, true);
      showInlineStatus('compareProgress', `语言列表加载失败：${dialectResult.reason?.message || dialectResult.reason}`, true);
    }

    if (qysResult.status === 'fulfilled') {
      populateEvolutionFilters(qysResult.value);
      updateActiveFilterCount();
    } else {
      console.error(qysResult.reason);
      showInlineStatus('evolutionProgress', `中古筛选项加载失败：${qysResult.reason?.message || qysResult.reason}`, true);
    }
  }

  function bindUi() {
    document.querySelectorAll('.tab').forEach((button) => {
      button.addEventListener('click', () => switchTab(button.dataset.tab));
    });

    $('runEvolution').addEventListener('click', runEvolution);
    $('evolutionFilter').addEventListener('input', renderEvolutionTable);
    $('evolutionBody').addEventListener('click', handleEvolutionRowClick);
    $('clearEvolutionFilters').addEventListener('click', () => {
      getEvolutionFilterIds().forEach(clearEvolutionFilter);
      updateActiveFilterCount();
      if (state.evolutionLastModernRows) runEvolution();
    });

    getEvolutionFilterIds().forEach((id) => {
      $(id).addEventListener('change', (event) => {
        if (MULTI_FILTER_IDS.has(id)) syncAllFilterSelection(id, event.target);
        if (id === 'filterDivision') syncDivisionSelection(event.target);
        if (MULTI_FILTER_IDS.has(id)) updateMultiSelectSummary(id);
        updateActiveFilterCount();
        if (state.evolutionLastModernRows) runEvolution();
      });
    });

    ['evolutionSourceDimension', 'evolutionModernMode'].forEach((id) => {
      $(id).addEventListener('change', () => {
        if (state.evolutionLastModernRows) runEvolution();
      });
    });

    $('evolutionGlidesAsFinal').addEventListener('change', () => {
      if (state.evolutionLastModernRows) runEvolution();
    });

    $('runCompare').addEventListener('click', runComparison);
    $('compareFilter').addEventListener('input', renderComparisonTable);
    $('showMissing').addEventListener('change', renderComparisonTable);
    $('showNativeMap').addEventListener('click', showNativeComparisonMap);
    $('compareMode').addEventListener('change', recalculateComparisonFromRaw);
    $('compareGlidesAsFinal').addEventListener('change', recalculateComparisonFromRaw);

    $('evolutionDialectSearch').addEventListener('focus', () => openLanguageMenu());
    $('evolutionDialectSearch').addEventListener('input', () => {
      $('evolutionDialect').value = '';
      renderLanguageMenu($('evolutionDialectSearch').value);
      openLanguageMenu(false);
    });
    $('evolutionDialectSearch').addEventListener('keydown', (event) => {
      if (event.key === 'Enter') {
        const first = $('evolutionDialectMenu').querySelector('[data-dialect-short]');
        if (first) {
          event.preventDefault();
          selectLanguage(first.dataset.dialectShort);
        }
      } else if (event.key === 'Escape') {
        closeLanguageMenu();
      }
    });
    $('evolutionDialectToggle').addEventListener('click', () => {
      const menu = $('evolutionDialectMenu');
      if (menu.classList.contains('hidden')) openLanguageMenu();
      else closeLanguageMenu();
    });
    $('evolutionDialectMenu').addEventListener('click', (event) => {
      const option = event.target.closest('[data-dialect-short]');
      if (option) selectLanguage(option.dataset.dialectShort);
    });
    document.addEventListener('click', (event) => {
      if (!$('evolutionLanguagePicker').contains(event.target)) closeLanguageMenu();
    });

  }


  function switchTab(tab) {
    document.querySelectorAll('.tab').forEach((button) => button.classList.toggle('active', button.dataset.tab === tab));
    $('evolutionPanel').classList.toggle('active', tab === 'evolution');
    $('comparePanel').classList.toggle('active', tab === 'compare');
  }

  async function loadDialects() {
    const rows = parseBridgeJson(callBridge('getLanguages'), '語言列表');
    state.dialects = rows.map((row, index) => parseDialectRow(row, index))
      .filter((item) => item.shortName && item.shortName !== '廣韻' && !item.isHistorical);
    // Android 端已按音典自身排序返回，這裏保持原順序。
    state.dialectByShort = new Map(state.dialects.map((item) => [item.shortName, item]));
    $('evolutionDialect').value = '';
    $('evolutionDialectSearch').value = '';
    renderLanguageMenu('');

  }

  function getDialectDisplayName(dialect) {
    const raw = String(dialect?.fullName || dialect?.shortName || '').trim();
    // 与音典“语言”字段一致，界面统一使用简体“话”，不再拼接“简称 — 全称”。
    return raw.replaceAll('話', '话');
  }

  function getDialectSearchText(dialect) {
    return [
      getDialectDisplayName(dialect), dialect.shortName, dialect.fullName,
      dialect.location,
    ].filter(Boolean).join(' ').toLowerCase();
  }

  function renderLanguageMenu(query = '') {
    const menu = $('evolutionDialectMenu');
    if (!menu) return;
    const keyword = String(query || '').trim().toLowerCase();
    const matches = state.dialects.filter((dialect) => !keyword || getDialectSearchText(dialect).includes(keyword));
    const selected = $('evolutionDialect').value;
    if (!matches.length) {
      menu.innerHTML = '<div class="language-empty">没有匹配的语言</div>';
      return;
    }
    menu.innerHTML = matches.map((dialect) => `
      <button class="language-option ${dialect.shortName === selected ? 'active' : ''}" type="button" role="option" data-dialect-short="${escapeHtml(dialect.shortName)}">
        ${escapeHtml(getDialectDisplayName(dialect))}
      </button>`).join('');
  }

  function openLanguageMenu(refresh = true) {
    if (refresh) renderLanguageMenu($('evolutionDialectSearch').value);
    $('evolutionDialectMenu').classList.remove('hidden');
    $('evolutionDialectSearch').setAttribute('aria-expanded', 'true');
  }

  function closeLanguageMenu() {
    $('evolutionDialectMenu').classList.add('hidden');
    $('evolutionDialectSearch').setAttribute('aria-expanded', 'false');
  }

  function selectLanguage(shortName, close = true) {
    const dialect = state.dialectByShort.get(shortName);
    if (!dialect) return;
    $('evolutionDialect').value = dialect.shortName;
    $('evolutionDialectSearch').value = getDialectDisplayName(dialect);
    renderLanguageMenu($('evolutionDialectSearch').value);
    if (close) closeLanguageMenu();
  }

  function parseDialectRow(row, originalIndex = 0) {
    let toneConfig = row?.toneConfig || {};
    if (typeof toneConfig === 'string') {
      try { toneConfig = JSON.parse(toneConfig); } catch { toneConfig = {}; }
    }
    return {
      fullName: row?.language || row?.fullName || '',
      shortName: row?.label || row?.shortName || '',
      coordinate: row?.coordinate || '',
      mapLevel: Number(row?.mapLevel || 0),
      location: row?.location || '',
      toneConfig: toneConfig && typeof toneConfig === 'object' ? toneConfig : {},
      isHistorical: String(row?.historical || '') !== '' && String(row?.historical || '') !== '0',
      originalIndex,
    };
  }

  function compareYindianOrder(a, b) {
    return (a.originalIndex ?? 0) - (b.originalIndex ?? 0);
  }

  async function runEvolution() {
    const dialectName = $('evolutionDialect').value;
    if (!dialectName) return showInlineStatus('evolutionProgress', '请先选择一种语言。', true);

    const button = $('runEvolution');
    button.disabled = true;
    showInlineStatus('evolutionProgress', '正在读取中古音系与现代语言数据……');
    $('evolutionResults').classList.add('hidden');
    $('evolutionSummary').classList.add('hidden');

    try {
      const dialect = state.dialectByShort.get(dialectName) || { shortName: dialectName, toneConfig: {} };
      const [guangyunEntries, modernRows] = await Promise.all([
        loadGuangyunEntries(),
        state.evolutionLastModernRows?.dialectName === dialectName
          ? Promise.resolve(state.evolutionLastModernRows.rows)
          : loadModernDialect(dialectName),
      ]);

      state.evolutionLastModernRows = { dialectName, rows: modernRows };
      populateEvolutionFilters(state.middleRows || []);

      showInlineStatus('evolutionProgress', '正在按所选中古条件筛选，并计算今音归并……');
      await yieldToBrowser();

      const modernByChar = groupModernRowsByChar(modernRows);
      const grouped = new Map();
      const matchedChars = new Set();
      const matchedPositions = new Set();
      const sourceDimension = $('evolutionSourceDimension').value;
      const modernMode = $('evolutionModernMode').value;
      const glidesAsFinal = $('evolutionGlidesAsFinal').checked;
      const filters = getEvolutionFilters();

      for (const entry of guangyunEntries) {
        const char = entry.char;
        const middle = entry.middle;
        const modern = modernByChar.get(char);
        if (!char || !middle || !modern?.length) continue;
        if (!matchesMiddleFilters(middle, filters)) continue;

        const oldValue = getMiddleDimensionValue(middle, sourceDimension);
        if (!oldValue) continue;

        for (const modernRow of modern) {
          for (const phonetic of expandPhonetics(modernRow['音標'])) {
            if (!phonetic) continue;
            const parts = decomposeModern(phonetic, dialect, { glidesAsFinal });
            const modernValue = getModernModeValue(parts, modernMode, dialect);
            if (!modernValue) continue;

            const key = `${oldValue}\u0000${modernValue}`;
            if (!grouped.has(key)) {
              grouped.set(key, {
                oldValue,
                modernValue,
                chars: new Set(),
                readings: new Set(),
                positions: new Set(),
                subtypes: new Set(),
              });
            }
            const group = grouped.get(key);
            group.chars.add(char);
            group.readings.add(phonetic);
            group.positions.add(middle.position);
            group.subtypes.add(formatMiddleSubtype(middle));
            matchedChars.add(char);
            matchedPositions.add(middle.position);
          }
        }
      }

      state.evolutionGroups = [...grouped.values()].sort((a, b) => {
        const oldCmp = compareMiddleValues(a.oldValue, b.oldValue, sourceDimension);
        return oldCmp || b.chars.size - a.chars.size || a.modernValue.localeCompare(b.modernValue, 'zh-CN');
      });
      state.evolutionMatchedChars = matchedChars;
      state.evolutionPositionCount = matchedPositions.size;

      $('summaryDialect').textContent = getDialectDisplayName(dialect);
      $('summaryChars').textContent = matchedChars.size.toLocaleString();
      $('summaryGroups').textContent = state.evolutionGroups.length.toLocaleString();
      $('summaryPositions').textContent = matchedPositions.size.toLocaleString();
      $('evolutionSummary').classList.remove('hidden');
      $('evolutionResults').classList.remove('hidden');
      hideInlineStatus('evolutionProgress');
      renderEvolutionTable();
    } catch (error) {
      console.error(error);
      showInlineStatus('evolutionProgress', humanizeDataError(error, '古今演化'), true);
    } finally {
      button.disabled = false;
    }
  }

  async function loadGuangyunEntries() {
    if (!state.guangyunEntries) {
      const rows = parseBridgeJson(callBridge('getGuangyunRows'), '廣韻');
      const entries = [];
      const middleRows = [];
      const seenMiddle = new Set();
      for (const row of rows) {
        const middle = parseMiddleRow({
          '切韻音系描述': row.description || '',
          '攝': row.she || '',
          '方音字彙描述': row.fanqieDescription || '',
          '廣韻韻目': row.guangyunRime || '',
        });
        if (!middle) continue;
        if (!seenMiddle.has(middle.position)) {
          seenMiddle.add(middle.position);
          middleRows.push(middle);
        }
        for (const char of Array.from(String(row.charGroup || '').replace(/\s+/g, ''))) {
          if (char) entries.push({ char, middle });
        }
      }
      state.guangyunEntries = entries;
      state.middleRows = middleRows;
    }
    return state.guangyunEntries;
  }

  async function loadParsedQysRimes() {
    await loadGuangyunEntries();
    return state.middleRows || [];
  }

  async function loadModernDialect(shortName) {
    const rows = parseBridgeJson(callBridge('getLanguageRows', shortName), `語言“${shortName}”`);
    return rows;
  }

  function groupModernRowsByChar(rows) {
    const map = new Map();
    for (const row of rows) {
      const charGroup = String(row['漢字'] || '').trim();
      if (!charGroup) continue;
      for (const char of new Set(Array.from(charGroup.replace(/\s+/g, '')))) {
        if (!map.has(char)) map.set(char, []);
        map.get(char).push(row);
      }
    }
    return map;
  }

  function parseMiddleRow(row) {
    const description = String(row['切韻音系描述'] || '').trim();
    if (!description) return null;
    const toneMatch = description.match(/[平上去入]$/);
    const tone = toneMatch?.[0] || '';
    const core = tone ? description.slice(0, -1) : description;
    const divisionMatch = core.match(/[一二三四]/);
    if (!divisionMatch || divisionMatch.index == null) return null;

    const initial = normalizeInitial(core.slice(0, divisionMatch.index).replace(/[開合]/g, ''));
    const afterDivision = core.slice(divisionMatch.index + 1);
    const divisionClassMatch = afterDivision.match(/^[ABC]/);
    const divisionClass = divisionClassMatch?.[0] || '';
    const rime = normalizeRime(divisionClass ? afterDivision.slice(1) : afterDivision);
    const fanqieDescription = String(row['方音字彙描述'] || '').trim();
    const opennessMatch = fanqieDescription.match(/[開合]/);
    const openness = opennessMatch?.[0] || '';
    const she = normalizeShe(String(row['攝'] || '').trim());
    const voicing = INITIAL_VOICING.get(initial) || '未分類';
    const division = divisionMatch[0];
    const position = `${initial}${division}${divisionClass}${rime}${tone}`;

    return {
      initial,
      voicing,
      rime,
      division,
      divisionClass: divisionClass || '—',
      openness: openness || '—',
      she: she || '—',
      tone,
      position,
      fanqieDescription,
      guangyunRime: String(row['廣韻韻目'] || '').trim(),
    };
  }

  function normalizeInitial(value) {
    const text = String(value || '').trim();
    return INITIAL_CANONICAL.get(text) || text;
  }

  function normalizeRime(value) {
    const text = String(value || '').trim();
    return RIME_CANONICAL.get(text) || text;
  }

  function normalizeShe(value) {
    return String(value || '').trim().replace(/[摄攝]$/, '')
      .replace('咸', '咸').replace('臻', '臻').replace('宕', '宕');
  }

  function buildInitialVoicingMap() {
    const map = new Map();
    const groups = {
      全清: ['幫', '端', '知', '精', '心', '莊', '生', '章', '書', '見', '影', '曉'],
      次清: ['滂', '透', '徹', '清', '初', '昌', '溪'],
      全濁: ['並', '定', '澄', '從', '邪', '崇', '俟', '常', '船', '群', '匣'],
      次濁: ['明', '泥', '娘', '日', '疑', '云', '以', '來'],
    };
    Object.entries(groups).forEach(([category, initials]) => {
      initials.forEach((initial) => map.set(initial, category));
    });
    return map;
  }

  function getEvolutionFilterIds() {
    return [
      'filterVoicing', 'filterDivision', 'filterOpenness',
      'filterShe', 'filterTone', 'filterInitial', 'filterRime',
    ];
  }

  function populateEvolutionFilters(rows) {
    if ($('filterInitial').dataset.ready === '1') return;

    renderFlatMultiFilter('filterVoicing', rows.map((r) => r.voicing), ['全清', '次清', '全濁', '次濁', '未分類']);
    renderDivisionFilter(rows);
    renderFlatMultiFilter('filterOpenness', rows.map((r) => r.openness).filter((value) => value === '開' || value === '合'), ['開', '合']);
    renderFlatMultiFilter('filterShe', rows.map((r) => r.she), SHE_ORDER);
    renderFlatMultiFilter('filterTone', rows.map((r) => r.tone), ['平', '上', '去', '入']);
    renderGroupedMultiFilter('filterInitial', rows.map((r) => r.initial), INITIAL_GROUPS);
    renderGroupedMultiFilter('filterRime', rows.map((r) => r.rime), RIME_GROUPS, true);

    getEvolutionFilterIds().forEach((id) => { $(id).dataset.ready = '1'; });
  }

  function renderFlatMultiFilter(id, values, preferredOrder = []) {
    const element = $(id);
    const selected = new Set(getMultiFilterValues(id));
    const useAll = selected.size === 0 || selected.has(ALL_FILTER_VALUE);
    const unique = [...new Set(values.filter(Boolean))];
    const ordered = [
      ...preferredOrder.filter((value) => unique.includes(value)),
      ...unique.filter((value) => !preferredOrder.includes(value)).sort((a, b) => a.localeCompare(b, 'zh-CN')),
    ];
    const options = element.querySelector('.multi-options');
    options.innerHTML = `
      <label class="multi-option multi-option-all">
        <input type="checkbox" data-filter-value="1" data-filter-all="1" value="${ALL_FILTER_VALUE}" ${useAll ? 'checked' : ''} />
        <span>全部</span>
      </label>` + ordered.map((value) => `
      <label class="multi-option">
        <input type="checkbox" data-filter-value="1" value="${escapeHtml(value)}" ${!useAll && selected.has(value) ? 'checked' : ''} />
        <span>${escapeHtml(value)}</span>
      </label>`).join('');
    updateMultiSelectSummary(id);
  }


  function renderDivisionFilter(rows) {
    const element = $('filterDivision');
    const selected = new Set(getMultiFilterValues('filterDivision'));
    const useAll = selected.size === 0 || selected.has(ALL_FILTER_VALUE);
    const available = new Set(rows.map((row) => getDivisionFilterValue(row)));
    const options = element.querySelector('.multi-options');
    const ordinary = (value, label) => available.has(value) ? `
      <label class="multi-option">
        <input type="checkbox" data-filter-value="1" value="${value}" ${!useAll && selected.has(value) ? 'checked' : ''} />
        <span>${label}</span>
      </label>` : '';
    const thirdValues = ['三A', '三B', '三C'].filter((value) => available.has(value));
    const allThirdChecked = !useAll && (selected.has('三') || (thirdValues.length > 0 && thirdValues.every((value) => selected.has(value))));
    options.innerHTML = [
      `<label class="multi-option multi-option-all">
        <input type="checkbox" data-filter-value="1" data-filter-all="1" value="${ALL_FILTER_VALUE}" ${useAll ? 'checked' : ''} />
        <span>全部</span>
      </label>`,
      ordinary('一', '一等'),
      ordinary('二', '二等'),
      thirdValues.length ? `
        <label class="multi-option multi-option-parent">
          <input type="checkbox" data-filter-value="1" data-division-parent="3" value="三" ${allThirdChecked ? 'checked' : ''} />
          <span>三等</span>
        </label>
        <div class="multi-suboptions">${thirdValues.map((value) => `
          <label class="multi-option multi-option-child">
            <input type="checkbox" data-filter-value="1" data-division-child="3" value="${value}" ${!useAll && selected.has(value) ? 'checked' : ''} />
            <span>${value}</span>
          </label>`).join('')}</div>` : '',
      ordinary('四', '四等'),
    ].join('');
    syncDivisionParentState();
    updateMultiSelectSummary('filterDivision');
  }

  function renderGroupedMultiFilter(id, values, groups, showAllKnown = false) {
    const element = $(id);
    const selected = new Set(getMultiFilterValues(id));
    const useAll = selected.size === 0 || selected.has(ALL_FILTER_VALUE);
    const available = new Set(values.filter(Boolean));
    const known = new Set(groups.flatMap((group) => group.values));
    const chunks = [
      `<label class="multi-option multi-option-all">
        <input type="checkbox" data-filter-value="1" data-filter-all="1" value="${ALL_FILTER_VALUE}" ${useAll ? 'checked' : ''} />
        <span>全部</span>
      </label>`
    ];
    for (const group of groups) {
      const present = showAllKnown ? group.values : group.values.filter((value) => available.has(value));
      if (!present.length) continue;
      chunks.push(`<div class="multi-group-title">${escapeHtml(group.title)}</div>`);
      chunks.push(present.map((value) => `
        <label class="multi-option">
          <input type="checkbox" data-filter-value="1" value="${escapeHtml(value)}" ${!useAll && selected.has(value) ? 'checked' : ''} />
          <span>${escapeHtml(value)}</span>
        </label>`).join(''));
    }
    const extras = [...available].filter((value) => !known.has(value)).sort((a, b) => a.localeCompare(b, 'zh-CN'));
    if (extras.length) {
      chunks.push('<div class="multi-group-title">其他</div>');
      chunks.push(extras.map((value) => `
        <label class="multi-option">
          <input type="checkbox" data-filter-value="1" value="${escapeHtml(value)}" ${!useAll && selected.has(value) ? 'checked' : ''} />
          <span>${escapeHtml(value)}</span>
        </label>`).join(''));
    }
    element.querySelector('.multi-options').innerHTML = chunks.join('');
    updateMultiSelectSummary(id);
  }

  function syncAllFilterSelection(id, target) {
    if (!(target instanceof HTMLInputElement)) return;
    const details = $(id);
    const all = details.querySelector('input[data-filter-all="1"]');
    if (!all) return;
    const specifics = [...details.querySelectorAll('input[data-filter-value="1"]')].filter((input) => input !== all);

    if (target === all) {
      if (all.checked) {
        specifics.forEach((input) => { input.checked = false; input.indeterminate = false; });
      } else if (!specifics.some((input) => input.checked)) {
        all.checked = true;
      }
      return;
    }

    if (target.checked) all.checked = false;
    if (!specifics.some((input) => input.checked)) all.checked = true;
  }

  function getDivisionFilterValue(middle) {
    if (middle.division === '三' && ['A', 'B', 'C'].includes(middle.divisionClass)) return `三${middle.divisionClass}`;
    return middle.division;
  }

  function syncDivisionSelection(target) {
    if (!(target instanceof HTMLInputElement)) return;
    const details = $('filterDivision');
    if (target.dataset.divisionParent === '3') {
      details.querySelectorAll('input[data-division-child="3"]').forEach((input) => { input.checked = target.checked; });
    }
    syncDivisionParentState();
  }

  function syncDivisionParentState() {
    const details = $('filterDivision');
    const all = details.querySelector('input[data-filter-all="1"]');
    const parent = details.querySelector('input[data-division-parent="3"]');
    const children = [...details.querySelectorAll('input[data-division-child="3"]')];
    if (!parent || !children.length) return;
    if (all?.checked) {
      parent.checked = false;
      parent.indeterminate = false;
      return;
    }
    const checkedCount = children.filter((input) => input.checked).length;
    parent.checked = checkedCount === children.length;
    parent.indeterminate = checkedCount > 0 && checkedCount < children.length;
  }

  function getMultiFilterValues(id) {
    return [...$(id).querySelectorAll('input[data-filter-value="1"]:checked')].map((input) => input.value);
  }

  function updateMultiSelectSummary(id) {
    const details = $(id);
    const values = getMultiFilterValues(id);
    const summary = details.querySelector('summary');
    if (!values.length || values.includes(ALL_FILTER_VALUE)) {
      summary.textContent = '全部';
      summary.title = '全部';
      return;
    }
    let displayValues = values.slice();
    if (id === 'filterDivision') {
      const third = ['三A', '三B', '三C'];
      if (displayValues.includes('三') || third.every((value) => displayValues.includes(value))) {
        displayValues = displayValues.filter((value) => value !== '三' && !third.includes(value));
        displayValues.splice(Math.min(2, displayValues.length), 0, '三等');
      }
      displayValues = displayValues.map((value) => ({ 一: '一等', 二: '二等', 四: '四等' }[value] || value));
    }
    const label = displayValues.length <= 3 ? displayValues.join('、') : `已選 ${displayValues.length} 項`;
    summary.textContent = label;
    summary.title = displayValues.join('、');
  }

  function clearEvolutionFilter(id) {
    const element = $(id);
    if (MULTI_FILTER_IDS.has(id)) {
      element.querySelectorAll('input[type="checkbox"]').forEach((input) => { input.checked = false; input.indeterminate = false; });
      const all = element.querySelector('input[data-filter-all="1"]');
      if (all) all.checked = true;
      if (id === 'filterDivision') syncDivisionParentState();
      updateMultiSelectSummary(id);
    } else {
      element.value = '';
    }
  }

  function getEvolutionFilters() {
    return {
      voicing: getMultiFilterValues('filterVoicing'),
      division: getMultiFilterValues('filterDivision'),
      openness: getMultiFilterValues('filterOpenness'),
      she: getMultiFilterValues('filterShe'),
      tone: getMultiFilterValues('filterTone'),
      initial: getMultiFilterValues('filterInitial'),
      rime: getMultiFilterValues('filterRime'),
    };
  }

  function updateActiveFilterCount() {
    const filters = getEvolutionFilters();
    const count = Object.values(filters).reduce((sum, value) => {
      if (Array.isArray(value)) return sum + value.filter((item) => item !== ALL_FILTER_VALUE).length;
      return sum + (value ? 1 : 0);
    }, 0);
    $('activeFilterCount').textContent = String(count);
    $('activeFilterCount').classList.toggle('active', count > 0);
  }

  function matchesMiddleFilters(middle, filters) {
    return Object.entries(filters).every(([key, value]) => {
      if (Array.isArray(value)) {
        if (!value.length || value.includes(ALL_FILTER_VALUE)) return true;
        if (key === 'division') {
          if (value.includes('三') && middle.division === '三') return true;
          return value.includes(getDivisionFilterValue(middle));
        }
        return value.includes(middle[key]);
      }
      return !value || middle[key] === value;
    });
  }

  function formatDivisionValue(middle) {
    if (middle.division === '三' && ['A', 'B', 'C'].includes(middle.divisionClass)) return `三${middle.divisionClass}`;
    return middle.division ? `${middle.division}等` : '—';
  }

  function getMiddleDimensionValue(middle, dimension) {
    if (dimension === 'division') return formatDivisionValue(middle);
    const value = middle[dimension];
    return value == null || value === '' ? '—' : value;
  }

  function getModernModeValue(parts, mode, dialect) {
    switch (mode) {
      case 'initial': return parts.initial;
      case 'final': return parts.final;
      case 'tone': return formatTone(parts.tone, dialect?.toneConfig || {});
      case 'notone': return parts.base || '∅';
      case 'full': return parts.original || '∅';
      default: return parts.original || '∅';
    }
  }

  function formatMiddleSubtype(middle) {
    const pieces = [
      middle.voicing,
      formatDivisionValue(middle),
      middle.openness,
      middle.she && middle.she !== '—' ? `${middle.she}攝` : middle.she,
      middle.tone,
    ].filter((item) => item && item !== '—');
    return `${middle.position}｜${pieces.join(' · ')}`;
  }

  function compareMiddleValues(a, b, dimension) {
    const orders = {
      voicing: ['全清', '次清', '全濁', '次濁', '未分類'],
      division: ['一等', '二等', '三A', '三B', '三C', '四等'],
      she: SHE_ORDER,
      initial: INITIAL_GROUPS.flatMap((group) => group.values),
      rime: RIME_GROUPS.flatMap((group) => group.values),
      openness: ['開', '合', '—'],
      tone: ['平', '上', '去', '入'],
    };
    const order = orders[dimension];
    if (order) {
      const ia = order.indexOf(a);
      const ib = order.indexOf(b);
      if (ia !== ib) return (ia < 0 ? 999 : ia) - (ib < 0 ? 999 : ib);
    }
    return a.localeCompare(b, 'zh-CN');
  }

  function expandPhonetics(value) {
    const text = String(value || '').trim();
    if (!text) return [];
    return [...new Set(text.split(/\s*\/\s*/).map((item) => item.trim()).filter(Boolean))];
  }

  function decomposeModern(phonetic, dialect, options = {}) {
    const original = String(phonetic || '').trim().normalize('NFC');
    const { base, tone } = splitTone(original, dialect?.toneConfig || {});
    const cleanBase = base.trim();
    if (!cleanBase) return { original, base: '', initial: '∅', final: '∅', tone };

    const graphemes = Array.from(cleanBase);
    const firstVowelIndex = graphemes.findIndex((ch) => VOWELS.has(ch));
    const syllabicIndex = graphemes.findIndex((ch) => SYLLABIC_MARKS.has(ch));

    if (firstVowelIndex < 0) {
      // 成音节辅音（m̩、ŋ̍ 等）以及未显式标记成音节性的单辅音读法，
      // 保守地按零声母 + 整体韵母处理，避免误把整节判成声母。
      return { original, base: cleanBase, initial: '∅', final: cleanBase, tone };
    }

    let onsetEnd = firstVowelIndex;
    if (options.glidesAsFinal !== false) {
      while (onsetEnd > 0 && GLIDES.has(graphemes[onsetEnd - 1])) onsetEnd -= 1;
      if (onsetEnd === 0 && GLIDES.has(graphemes[0])) onsetEnd = 0;
    }

    // 如果辅音本身带成音节符号且出现在第一个元音之前，说明其不应算普通声母。
    if (syllabicIndex >= 0 && syllabicIndex < firstVowelIndex) onsetEnd = 0;

    const initial = onsetEnd === 0 ? '∅' : graphemes.slice(0, onsetEnd).join('');
    const final = graphemes.slice(onsetEnd).join('') || '∅';
    return { original, base: cleanBase, initial, final, tone };
  }

  function splitTone(phonetic, toneConfig = {}) {
    const original = String(phonetic || '').trim().normalize('NFC');
    const toneKeys = Object.keys(toneConfig || {}).filter(Boolean).sort((a, b) => b.length - a.length);
    for (const key of toneKeys) {
      if (original.endsWith(key)) {
        return { base: original.slice(0, -key.length), tone: key };
      }
    }
    const match = original.match(/([0-9¹²³⁴⁵⁶⁷⁸⁹⁰˥˦˧˨˩]+)$/u);
    if (match) return { base: original.slice(0, -match[1].length), tone: match[1] };
    return { base: original, tone: '' };
  }

  function formatTone(tone, toneConfig) {
    if (!tone) return '∅';
    const config = toneConfig?.[tone];
    if (Array.isArray(config) && config[0] != null && String(config[0]).trim()) {
      return `${tone} / ${config[0]}`;
    }
    return tone;
  }

  function renderEvolutionTable() {
    const sourceDimension = $('evolutionSourceDimension').value;
    const modernMode = $('evolutionModernMode').value;
    const oldLabel = MIDDLE_DIMENSION_LABELS[sourceDimension] || sourceDimension;
    const modernLabel = MODERN_MODE_LABELS[modernMode] || modernMode;
    const filter = $('evolutionFilter').value.trim().toLowerCase();
    const groups = state.evolutionGroups.filter((group) => {
      if (!filter) return true;
      const haystack = [
        group.oldValue, group.modernValue,
        ...group.readings, ...group.chars, ...group.positions, ...group.subtypes,
      ].join(' ').toLowerCase();
      return haystack.includes(filter);
    });

    $('evolutionTableTitle').textContent = `${oldLabel} → 今音${modernLabel}`;
    $('evolutionTableNote').textContent = `当前中古条件：${formatActiveFilters()}。点击“查看详情”可查看完整例字和所含中古音韵地位。`;
    $('evolutionHead').innerHTML = `<tr>
      <th>${escapeHtml(oldLabel)}</th>
      <th>今音${escapeHtml(modernLabel)}</th>
      <th>字数</th>
      <th>涉及音韵地位数</th>
      <th>例字</th>
      <th>现代完整读音</th>
      <th></th>
    </tr>`;

    const body = $('evolutionBody');
    body.innerHTML = groups.map((group) => {
      const originalIndex = state.evolutionGroups.indexOf(group);
      const chars = [...group.chars];
      const preview = chars.slice(0, 22).join('');
      const suffix = chars.length > 22 ? ` … +${chars.length - 22}` : '';
      const readings = [...group.readings].slice(0, 10).join(' · ');
      return `<tr>
        <td><strong>${escapeHtml(group.oldValue)}</strong></td>
        <td class="ipa"><strong>${escapeHtml(group.modernValue)}</strong></td>
        <td>${group.chars.size.toLocaleString()}</td>
        <td>${group.positions.size.toLocaleString()}</td>
        <td class="example-chars">${escapeHtml(preview)}${escapeHtml(suffix)}</td>
        <td class="ipa">${escapeHtml(readings)}${group.readings.size > 10 ? ' …' : ''}</td>
        <td><button class="small-button" type="button" data-group-index="${originalIndex}">查看详情</button></td>
      </tr>`;
    }).join('');

    if (!groups.length) {
      body.innerHTML = `<tr><td colspan="7" style="text-align:center;color:#64748b;padding:32px">没有符合当前中古筛选条件的对应关系。</td></tr>`;
    }
  }

  function formatActiveFilters() {
    const labels = {
      voicing: '清濁', division: '等', openness: '開合',
      she: '攝', tone: '聲調', initial: '聲母', rime: '韻母',
    };
    const active = Object.entries(getEvolutionFilters())
      .filter(([, value]) => Array.isArray(value) ? value.length > 0 : Boolean(value))
      .map(([key, value]) => {
        let shown = Array.isArray(value) ? value.slice() : value;
        if (key === 'division' && Array.isArray(shown)) {
          const thirds = ['三A', '三B', '三C'];
          if (shown.includes('三') || thirds.every((item) => shown.includes(item))) {
            shown = shown.filter((item) => item !== '三' && !thirds.includes(item));
            shown.push('三等');
          }
          shown = shown.map((item) => ({ 一: '一等', 二: '二等', 四: '四等' }[item] || item));
        }
        const text = Array.isArray(shown) ? shown.join('、') : shown;
        return `${labels[key]}=${text}`;
      });
    return active.length ? active.join('；') : '全部';
  }

  function handleEvolutionRowClick(event) {
    const button = event.target.closest('[data-group-index]');
    if (!button) return;
    const group = state.evolutionGroups[Number(button.dataset.groupIndex)];
    if (!group) return;
    const sourceDimension = $('evolutionSourceDimension').value;
    const modernMode = $('evolutionModernMode').value;
    const oldLabel = MIDDLE_DIMENSION_LABELS[sourceDimension] || sourceDimension;
    const modernLabel = MODERN_MODE_LABELS[modernMode] || modernMode;
    $('dialogTitle').textContent = `${group.oldValue} → ${group.modernValue}`;
    $('dialogSubtitle').textContent = `${oldLabel} → 今音${modernLabel} · ${group.chars.size} 字 · ${group.positions.size} 种中古音韵地位`;
    $('dialogPositions').innerHTML = [...group.subtypes]
      .sort((a, b) => a.localeCompare(b, 'zh-CN'))
      .map((position) => `<span class="position-chip">${escapeHtml(position)}</span>`)
      .join('');
    $('dialogReadings').innerHTML = [...group.readings]
      .sort((a, b) => a.localeCompare(b))
      .map((reading) => `<span class="reading-chip ipa">${escapeHtml(reading)}</span>`)
      .join('');
    $('dialogChars').textContent = [...group.chars].join(' ');
    const dialog = $('charsDialog');
    if (typeof dialog.showModal === 'function') dialog.showModal();
    else dialog.setAttribute('open', '');
  }

  async function runComparison() {
    if (!state.dialects.length) return showInlineStatus('compareProgress', '语言列表尚未加载完成。', true);
    const chars = parseInputChars($('compareChars').value);
    if (chars.length < 2) return showInlineStatus('compareProgress', '请至少输入两个汉字。', true);
    if (chars.length > 10) return showInlineStatus('compareProgress', 'MCPDict 查询接口一次最多比较 10 个汉字。', true);

    const button = $('runCompare');
    button.disabled = true;
    showInlineStatus('compareProgress', '正在查询 MCPDict 并计算各语言的分合关系……');
    $('compareSummary').classList.add('hidden');
    $('mapCard').classList.add('hidden');
    $('compareResults').classList.add('hidden');

    try {
      const rows = await queryCharacters(chars);
      state.compareChars = chars;
      state.compareRawRows = rows;
      recalculateComparisonFromRaw();
      $('compareSummary').classList.remove('hidden');
      $('mapCard').classList.remove('hidden');
      $('compareResults').classList.remove('hidden');
      hideInlineStatus('compareProgress');
    } catch (error) {
      console.error(error);
      showInlineStatus('compareProgress', humanizeDataError(error, '字音比较'), true);
    } finally {
      button.disabled = false;
    }
  }

  function recalculateComparisonFromRaw() {
    if (!state.compareRawRows.length || !state.compareChars.length) return;
    const chars = state.compareChars;
    const rows = state.compareRawRows;
    const mode = $('compareMode').value;
    const glidesAsFinal = $('compareGlidesAsFinal').checked;
    const byDialect = new Map();
    for (const row of rows) {
      const name = row['語言'];
      if (!name) continue;
      if (!byDialect.has(name)) byDialect.set(name, []);
      byDialect.get(name).push(row);
    }

    const results = [];
    for (const dialect of state.dialects) {
      const dialectRows = byDialect.get(dialect.shortName) || [];
      const readingsByChar = chars.map((char) => {
        const relevant = dialectRows.filter((row) => String(row['字組'] || '').includes(char));
        const readings = relevant.flatMap((row) => extractReadings(row['讀音']));
        return [...new Set(readings)].sort((a, b) => a.localeCompare(b));
      });
      const normalizedByChar = readingsByChar.map((readings) => readings.map((reading) => ({
        raw: reading,
        parts: decomposeModern(reading, dialect, { glidesAsFinal }),
      })));
      const status = classifyReadings(normalizedByChar, mode, dialect);
      results.push({ dialect, readingsByChar, normalizedByChar, status });
    }

    state.compareRows = results;
    updateComparisonSummary();
    renderComparisonTable();
    $('mapModeNote').textContent = `当前按“${MODERN_MODE_LABELS[mode]}”判断同异；点击地图点查看完整读音。`;
  }

  function parseInputChars(input) {
    const compact = String(input || '').replace(/[\s,，、;；/]+/g, '');
    return [...new Set(Array.from(compact))];
  }

  async function queryCharacters(chars) {
    return parseBridgeJson(callBridge('queryChars', chars.join('')), '字音比較');
  }

  function extractReadings(raw) {
    return String(raw || '')
      .split(/\t+/)
      .map((part) => part.split('{')[0].trim())
      .filter(Boolean);
  }

  function classifyReadings(normalizedByChar, mode, dialect) {
    if (normalizedByChar.some((readings) => readings.length === 0)) return 'missing';
    const sets = normalizedByChar.map((readings) => new Set(readings.map(({ parts }) => normalizedValueFromParts(parts, mode, dialect))));
    const signatures = sets.map((set) => [...set].sort().join(' / '));
    if (new Set(signatures).size === 1) return 'same';

    let hasOverlap = false;
    for (let i = 0; i < sets.length && !hasOverlap; i += 1) {
      for (let j = i + 1; j < sets.length && !hasOverlap; j += 1) {
        hasOverlap = [...sets[i]].some((value) => sets[j].has(value));
      }
    }

    if (hasOverlap || new Set(signatures).size < sets.length) return 'partial';
    return 'different';
  }

  function normalizedValueFromParts(parts, mode, dialect) {
    switch (mode) {
      case 'initial': return parts.initial;
      case 'final': return parts.final;
      case 'tone': return parts.tone || '∅';
      case 'notone': return parts.base || '∅';
      case 'full':
      default: return parts.original || '∅';
    }
  }

  function displayValueFromParts(parts, mode, dialect) {
    if (mode === 'tone') return formatTone(parts.tone, dialect?.toneConfig || {});
    return normalizedValueFromParts(parts, mode, dialect);
  }

  function updateComparisonSummary() {
    const counts = { same: 0, partial: 0, different: 0, missing: 0 };
    for (const row of state.compareRows) counts[row.status] += 1;
    $('sameCount').textContent = counts.same.toLocaleString();
    $('partialCount').textContent = counts.partial.toLocaleString();
    $('differentCount').textContent = counts.different.toLocaleString();
    $('missingCount').textContent = counts.missing.toLocaleString();
  }

  function renderComparisonTable() {
    const chars = state.compareChars;
    const mode = $('compareMode').value;
    const filter = $('compareFilter').value.trim().toLowerCase();
    const showMissing = $('showMissing').checked;
    const rows = state.compareRows.filter((row) => {
      if (!showMissing && row.status === 'missing') return false;
      if (!filter) return true;
      const dialect = row.dialect;
      return [dialect.shortName, dialect.fullName, dialect.location]
        .join(' ').toLowerCase().includes(filter);
    });

    $('compareHead').innerHTML = `<tr>
      <th>语言</th>
      ${chars.map((char) => `<th>${escapeHtml(char)}</th>`).join('')}
      <th>结果</th>
    </tr>`;

    const visible = rows.slice(0, 600);
    $('compareBody').innerHTML = visible.map((row) => {
      const d = row.dialect;
      const region = d.location || '';
      return `<tr>
        <td><strong>${escapeHtml(getDialectDisplayName(d))}</strong>${region ? `<br><small class="muted-small">${escapeHtml(region)}</small>` : ''}</td>
        ${row.normalizedByChar.map((items) => `<td class="ipa">${renderReadingCell(items, mode, d)}</td>`).join('')}
        <td><span class="status-tag ${row.status}">${STATUS_LABELS[row.status]}</span></td>
      </tr>`;
    }).join('');

    if (!visible.length) {
      $('compareBody').innerHTML = `<tr><td colspan="${chars.length + 2}" style="text-align:center;color:#64748b;padding:32px">没有符合当前条件的语言。</td></tr>`;
    }

    const parserNote = $('compareGlidesAsFinal').checked ? 'j/w/ɥ 按介音归韵母' : 'j/w/ɥ 可计入声母';
    $('compareTableNote').textContent = rows.length > 600
      ? `按“${MODERN_MODE_LABELS[mode]}”比较；${parserNote}。共有 ${rows.length.toLocaleString()} 个结果，表格只显示前 600 个。`
      : `按“${MODERN_MODE_LABELS[mode]}”比较；${parserNote}。当前显示 ${rows.length.toLocaleString()} 种语言。`;
  }

  function renderReadingCell(items, mode, dialect) {
    if (!items.length) return '—';
    return items.map(({ raw, parts }) => {
      const compareValue = displayValueFromParts(parts, mode, dialect);
      const split = `声 ${parts.initial} · 韵 ${parts.final} · 调 ${formatTone(parts.tone, dialect?.toneConfig || {})}`;
      if (mode === 'full') {
        return `<div class="reading-item"><span class="reading-raw">${escapeHtml(raw)}</span><span class="reading-split">${escapeHtml(split)}</span></div>`;
      }
      return `<div class="reading-item"><span class="reading-raw">${escapeHtml(raw)}</span><span class="compare-value">${escapeHtml(MODERN_MODE_LABELS[mode])}: ${escapeHtml(compareValue)}</span><span class="reading-split">${escapeHtml(split)}</span></div>`;
    }).join('');
  }

  function showNativeComparisonMap() {
    if (!state.compareRows.length) return;
    const showMissing = $('showMissing').checked;
    const mode = $('compareMode').value;
    const rows = state.compareRows
      .filter((row) => showMissing || row.status !== 'missing')
      .filter((row) => row.dialect.coordinate)
      .map((row) => {
        const readings = state.compareChars.map((char, index) => {
          const raw = (row.readingsByChar[index] || []).join(' / ') || '—';
          const norm = (row.normalizedByChar[index] || [])
            .map(({ parts }) => displayValueFromParts(parts, mode, row.dialect))
            .filter(Boolean).join(' / ') || '—';
          return { char, raw, norm };
        });
        const detail = readings.map((item) => {
          const extra = mode !== 'full' && item.norm !== item.raw ? `（${item.norm}）` : '';
          return `${item.char} ${item.raw}${extra}`;
        }).join('　');
        return {
          label: getDialectDisplayName(row.dialect),
          coordinate: row.dialect.coordinate,
          mapLevel: row.dialect.mapLevel || 0,
          status: row.status,
          ipa: readings.map((item) => `${item.char}${item.raw}`).join('  '),
          detail: `${STATUS_LABELS[row.status]} · ${MODERN_MODE_LABELS[mode]}\n${detail}`,
        };
      });
    if (!rows.length) {
      showInlineStatus('compareProgress', '當前結果沒有可用的地圖座標。', true);
      return;
    }
    callBridge('showCompareMap', JSON.stringify(rows));
  }



  function showInlineStatus(id, message, isError = false) {
    const node = $(id);
    node.textContent = message;
    node.classList.remove('hidden');
    node.classList.toggle('error', isError);
  }

  function hideInlineStatus(id) {
    $(id).classList.add('hidden');
  }

  function humanizeDataError(error, featureName) {
    const message = String(error?.message || error || '未知錯誤');
    return `${featureName}運行失敗：${message}`;
  }

  function yieldToBrowser() {
    return new Promise((resolve) => setTimeout(resolve, 0));
  }
})();
