-- V25에서 JSON 경로가 잘못되어 imageUrl이 빈 문자열로 남아있는 문제 수정
-- body_data는 JSON_ARRAY로 직접 저장되므로 $[index] 형태로 접근해야 함

-- ETF-01: '간단한 사례' 섹션 (order=4, 배열 인덱스 3) IMAGE imageUrl 수정
UPDATE content
SET body_data = JSON_REPLACE(
    body_data,
    '$[3].content[0].imageUrl',
    'https://finq-assets.s3.ap-northeast-2.amazonaws.com/img/ETF-01-4.png'
)
WHERE content_code = 'ETF-01';

-- TAX-09: '연금저축과 비교하면' 섹션 (order=5, 배열 인덱스 4) IMAGE imageUrl 수정
UPDATE content
SET body_data = JSON_REPLACE(
    body_data,
    '$[4].content[0].imageUrl',
    'https://finq-assets.s3.ap-northeast-2.amazonaws.com/img/ETF-09-5.png'
)
WHERE content_code = 'TAX-09';