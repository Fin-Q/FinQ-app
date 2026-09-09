-- SAL-04: block order 3의 bodyType을 CASE → COMPARISON으로 변경 (tableImageUrl 사용을 위해)
UPDATE content SET body_data = REPLACE(body_data,
    '{"bodyType":"CASE","order":3,"title":"이자를 받는 기간이 달라요"',
    '{"bodyType":"COMPARISON","order":3,"title":"이자를 받는 기간이 달라요"')
WHERE content_code = 'SAL-04';

-- TAX-09: block order 3의 bodyType을 CASE → COMPARISON으로 변경 (tableImageUrl 사용을 위해)
UPDATE content SET body_data = REPLACE(body_data,
    '{"bodyType":"CASE","order":3,"title":"연금저축과 비교하면"',
    '{"bodyType":"COMPARISON","order":3,"title":"연금저축과 비교하면"')
WHERE content_code = 'TAX-09';
