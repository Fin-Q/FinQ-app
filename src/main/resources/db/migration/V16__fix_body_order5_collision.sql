-- BODY block order=5가 SUMMARY_BLOCK_ORDER=5와 충돌하는 콘텐츠 수정
-- BODY order 5 → 7로 변경 (SUMMARY=5, F=6 이후)

-- SAL-04
UPDATE content SET body_data = REPLACE(body_data,
    '"bodyType":"CASE","order":5,"title":"금리만 비교하면 안 되는 이유"',
    '"bodyType":"CASE","order":7,"title":"금리만 비교하면 안 되는 이유"')
WHERE content_code = 'SAL-04';

-- ETF-02
UPDATE content SET body_data = REPLACE(body_data,
    '"bodyType":"CASE","order":5,"title":"자주 만나게 되는 지수 예시"',
    '"bodyType":"CASE","order":7,"title":"자주 만나게 되는 지수 예시"')
WHERE content_code = 'ETF-02';

-- ETF-06
UPDATE content SET body_data = REPLACE(body_data,
    '"bodyType":"CASE","order":5,"title":"같은 이름 안에서도 다른 정보"',
    '"bodyType":"CASE","order":7,"title":"같은 이름 안에서도 다른 정보"')
WHERE content_code = 'ETF-06';

-- TAX-03
UPDATE content SET body_data = REPLACE(body_data,
    '"bodyType":"CASE","order":5,"title":"손익을 함께 보는 경우"',
    '"bodyType":"CASE","order":7,"title":"손익을 함께 보는 경우"')
WHERE content_code = 'TAX-03';

-- TAX-04
UPDATE content SET body_data = REPLACE(body_data,
    '"bodyType":"CASE","order":5,"title":"같은 S&P500이어도"',
    '"bodyType":"CASE","order":7,"title":"같은 S&P500이어도"')
WHERE content_code = 'TAX-04';

-- TAX-07
UPDATE content SET body_data = REPLACE(body_data,
    '"bodyType":"CASE","order":5,"title":"계좌 하나 안에서 보면"',
    '"bodyType":"CASE","order":7,"title":"계좌 하나 안에서 보면"')
WHERE content_code = 'TAX-07';

-- TAX-09
UPDATE content SET body_data = REPLACE(body_data,
    '"bodyType":"CASE","order":5,"title":"어느 쪽이 더 좋을까요?"',
    '"bodyType":"CASE","order":7,"title":"어느 쪽이 더 좋을까요?"')
WHERE content_code = 'TAX-09';
