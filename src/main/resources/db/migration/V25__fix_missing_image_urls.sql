-- ETF-01: '간단한 사례' 섹션 IMAGE imageUrl 누락 수정
UPDATE content
SET body_data = JSON_REPLACE(
    body_data,
    '$.sections[3].content[0].imageUrl',
    'https://finq-assets.s3.ap-northeast-2.amazonaws.com/img/ETF-01-4.png'
)
WHERE content_code = 'ETF-01';

-- TAX-09: '연금저축과 비교하면' 섹션 IMAGE imageUrl 누락 수정
UPDATE content
SET body_data = JSON_REPLACE(
    body_data,
    '$.sections[4].content[0].imageUrl',
    'https://finq-assets.s3.ap-northeast-2.amazonaws.com/img/ETF-09-5.png'
)
WHERE content_code = 'TAX-09';