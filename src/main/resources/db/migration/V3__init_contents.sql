INSERT INTO content (content_code, category_id, title, description, source, reference_date, display_order, body_data, summary_content, is_premium, created_at, updated_at) VALUES
-- SAL 카테고리 (category_id=1)
('SAL-01', 1, '월급 관리의 시작', '월급 관리, 어디서부터 시작할까요?', '금융감독원', '2026-01-01', 1,
 '[{"bodyType":"EXPLANATION","order":1,"title":"월급은 들어왔는데 왜 매달 남는 돈이 없을까요?","description":"월급이 얼마인지는 바로 떠오르는데, 지난달에 어디에 얼마를 썼는지는 생각보다 잘 모를 수 있어요. 돈이 왜 남지 않는지 알고 싶다면 먼저 들어온 돈과 나간 돈의 흐름부터 확인해야 해요.","additionalDescription":"돈이 들어오고 나가는 흐름을 현금흐름이라고 해요. 들어오는 돈은 수입, 나가는 돈은 지출, 남는 돈은 수입 - 지출이에요. 월급이 같더라도 지출이 달라지면 매달 남는 돈도 달라질 수 있어요."},{"bodyType":"CASE","order":3,"title":"지출을 나눠서 보기","description":"지출을 비슷한 항목끼리 나눠보면 내 돈이 어디로 나가고 있는지 확인하기 쉬워져요. 월급이 250만 원인 가상의 직장인을 볼게요. 월세·관리비 60만 원, 통신비·보험료 22만 원, 교통비 8만 원, 식비 60만 원, 카페·간식 15만 원, 쇼핑 30만 원, 여가·모임 20만 원. 총지출 215만 원 → 남은 돈 35만 원. 월급이 250만 원이어도 얼마를 지출했느냐에 따라 남는 돈은 달라져요. 그래서 월급 금액만 보는 것보다 수입과 지출을 함께 보는 것이 중요해요.","imageUrl":"https://example.com/images/case-spending.png"}]',
 '돈을 관리하려면 얼마를 버는지만 보는 것으로는 부족해요. 들어온 돈과 나간 돈을 함께 확인해야 한 달 동안 내 돈이 어떻게 움직였는지 알 수 있어요. 현금흐름 = 들어오는 돈과 나가는 돈의 흐름.',
 false, NOW(), NOW()),
('SAL-02', 1, '고정비와 변동비', '지출을 고정비와 변동비로 나눠 관리해보세요.', NULL, NULL, 2, NULL, NULL, false, NOW(), NOW()),
('SAL-03', 1, '비상자금 준비하기', '예상치 못한 지출에 대비하는 비상자금을 알아보세요.', NULL, NULL, 3, NULL, NULL, false, NOW(), NOW()),
('SAL-04', 1, '예금과 적금 이해하기', '예금과 적금의 차이, 제대로 이해하고 시작하세요.', NULL, NULL, 4, NULL, NULL, false, NOW(), NOW()),
('SAL-05', 1, '목적별 자금 관리', '돈의 목적과 시점에 따라 자금을 나눠 관리해보세요.', NULL, NULL, 5, NULL, NULL, false, NOW(), NOW()),
-- INV 카테고리 (category_id=2)
('INV-01', 2, '수익률과 위험', '투자에서 수익률과 위험의 관계를 알아보세요.', NULL, NULL, 1, NULL, NULL, false, NOW(), NOW()),
('INV-02', 2, '복리의 이해', '복리의 개념과 시간의 힘을 이해해보세요.', NULL, NULL, 2, NULL, NULL, false, NOW(), NOW()),
('INV-03', 2, '투자 비용', '투자 비용이 장기 수익에 미치는 영향을 살펴보세요.', NULL, NULL, 3, NULL, NULL, false, NOW(), NOW()),
('INV-04', 2, '분산투자', '분산투자로 위험을 나누는 방법을 알아보세요.', NULL, NULL, 4, NULL, NULL, false, NOW(), NOW()),
('INV-05', 2, '적립식 투자', '적립식 투자의 특징과 원리를 이해해보세요.', NULL, NULL, 5, NULL, NULL, false, NOW(), NOW()),
-- STK 카테고리 (category_id=3)
('ETF-01', 3, '주식이란', '주식의 기본 개념과 주주의 의미를 알아보세요.', NULL, NULL, 1, NULL, NULL, false, NOW(), NOW()),
('ETF-02', 3, '주가지수 이해하기', '주가지수가 무엇이고 어떻게 읽는지 알아보세요.', NULL, NULL, 2, NULL, NULL, false, NOW(), NOW()),
('ETF-03', 3, 'ETF의 기본', 'ETF의 기본 구조와 특징을 이해해보세요.', NULL, NULL, 3, NULL, NULL, false, NOW(), NOW()),
('ETF-04', 3, 'ETF 비용 이해하기', 'ETF 비용이 투자 결과에 미치는 영향을 알아보세요.', NULL, NULL, 4, NULL, NULL, false, NOW(), NOW()),
('ETF-05', 3, 'ETF 이름 읽기', 'ETF 이름에서 핵심 정보를 읽는 방법을 알아보세요.', NULL, NULL, 5, NULL, NULL, false, NOW(), NOW()),
('ETF-06', 3, 'ETF 유형과 옵션', '패시브와 액티브, 환헤지 등 ETF 유형을 이해해보세요.', NULL, NULL, 6, NULL, NULL, false, NOW(), NOW()),
('ETF-07', 3, 'ETF 비교하기', '같은 지수를 추종하는 ETF를 비교하는 방법을 알아보세요.', NULL, NULL, 7, NULL, NULL, false, NOW(), NOW()),
-- TAX 카테고리 (category_id=4)
('TAX-01', 4, '금융소득이란', '이자, 배당 등 금융소득의 개념을 알아보세요.', NULL, NULL, 1, NULL, NULL, false, NOW(), NOW()),
('TAX-02', 4, '세전과 세후', '세전과 세후의 차이를 이해해보세요.', NULL, NULL, 2, NULL, NULL, false, NOW(), NOW()),
('TAX-03', 4, '주식 매매 세금', '국내·해외 주식의 세금 구조를 비교해보세요.', NULL, NULL, 3, NULL, NULL, false, NOW(), NOW()),
('TAX-04', 4, 'ETF 세금', 'ETF 유형별 세금 구조의 차이를 알아보세요.', NULL, NULL, 4, NULL, NULL, false, NOW(), NOW()),
('TAX-05', 4, '절세계좌란', '절세계좌의 개념과 세제 혜택을 이해해보세요.', NULL, NULL, 5, NULL, NULL, false, NOW(), NOW()),
('TAX-06', 4, '과세이연과 비과세', '과세이연과 비과세의 차이를 알아보세요.', NULL, NULL, 6, NULL, NULL, false, NOW(), NOW()),
('TAX-07', 4, 'ISA 계좌', 'ISA 계좌의 특징과 활용법을 알아보세요.', NULL, NULL, 7, NULL, NULL, false, NOW(), NOW()),
('TAX-08', 4, '연금저축', '연금저축의 세제 혜택과 조건을 이해해보세요.', NULL, NULL, 8, NULL, NULL, false, NOW(), NOW()),
('TAX-09', 4, 'IRP 계좌', 'IRP의 특징과 연금저축과의 차이를 알아보세요.', NULL, NULL, 9, NULL, NULL, false, NOW(), NOW());
