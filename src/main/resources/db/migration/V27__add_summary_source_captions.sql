-- 핵심 정리 블록에 출처 CAPTION 추가 (DEEP 제외)

-- SAL-01 ~ SAL-05: 금융감독원 | 「생애주기별 금융생활 가이드북」 | 2025
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 금융감독원 | 「생애주기별 금융생활 가이드북」 | 2025"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'SAL-01';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 금융감독원 | 「생애주기별 금융생활 가이드북」 | 2025"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'SAL-02';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 금융감독원 | 「생애주기별 금융생활 가이드북」 | 2025"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'SAL-03';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 금융감독원 | 「생애주기별 금융생활 가이드북」 | 2025"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'SAL-04';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 금융감독원 | 「생애주기별 금융생활 가이드북」 | 2025"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'SAL-05';

-- INV-01: 금융감독원 | 「대학생을 위한 실용금융」 | 2024
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 금융감독원 | 「대학생을 위한 실용금융」 | 2024"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'INV-01';

-- INV-02: 한국은행 | 「경제금융용어 700선」 | 2020
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 한국은행 | 「경제금융용어 700선」 | 2020"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'INV-02';

-- INV-03: 금융투자협회 | 「현명한 펀드투자방법」 | 2026.09.01 기준
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 금융투자협회 | 「현명한 펀드투자방법」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'INV-03';

-- INV-04: 신한투자증권 | 「ETF가이드」 | 2026.09.01 기준
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 신한투자증권 | 「ETF가이드」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'INV-04';

-- INV-05: 재정경제부 | 「경제배움e+」 | 2026.09.01 기준
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 재정경제부 | 「경제배움e+」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'INV-05';

-- ETF-01 ~ ETF-03: 한국은행 | 「경제금융용어 700선」 | 2020
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 한국은행 | 「경제금융용어 700선」 | 2020"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'ETF-01';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 한국은행 | 「경제금융용어 700선」 | 2020"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'ETF-02';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 한국은행 | 「경제금융용어 700선」 | 2020"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'ETF-03';

-- ETF-04 ~ ETF-07: 신한투자증권 | 「ETF가이드」 | 2026.09.01 기준
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 신한투자증권 | 「ETF가이드」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'ETF-04';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 신한투자증권 | 「ETF가이드」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'ETF-05';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 신한투자증권 | 「ETF가이드」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'ETF-06';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 신한투자증권 | 「ETF가이드」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'ETF-07';

-- TAX-01 ~ TAX-02: 법제처 국가법령정보센터 | 「소득세법」·「지방세법」 | 2026.09.01 기준
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 법제처 국가법령정보센터 | 「소득세법」·「지방세법」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'TAX-01';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 법제처 국가법령정보센터 | 「소득세법」·「지방세법」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'TAX-02';

-- TAX-03: 국세청 | 「2026년 해외주식과 세금」 | 2026.05 발간
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 국세청 | 「2026년 해외주식과 세금」 | 2026.05 발간"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'TAX-03';

-- TAX-04: 법제처 국가법령정보센터 | 관련 세법 | 2026.09.01 기준
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 법제처 국가법령정보센터 | 관련 세법 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'TAX-04';

-- TAX-05: 법제처 국가법령정보센터 | 「조세특례제한법」 | 2026.09.01 기준
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 법제처 국가법령정보센터 | 「조세특례제한법」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'TAX-05';

-- TAX-06: 법제처 국가법령정보센터 | 「소득세법」 | 2026.09.01 기준
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 법제처 국가법령정보센터 | 「소득세법」 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'TAX-06';

-- TAX-07 ~ TAX-09: 법제처 국가법령정보센터 | 관련 세법 | 2026.09.01 기준
UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 법제처 국가법령정보센터 | 관련 세법 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'TAX-07';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 법제처 국가법령정보센터 | 관련 세법 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'TAX-08';

UPDATE content SET body_data = JSON_ARRAY_APPEND(
    body_data,
    REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_data, 'one', '핵심 정리', NULL, '$[*].title')), '.title', '.content'),
    CAST('{"type":"CAPTION","text":"출처 법제처 국가법령정보센터 | 관련 세법 | 2026.09.01 기준"}' AS JSON)
), updated_at = NOW()
WHERE content_code = 'TAX-09';