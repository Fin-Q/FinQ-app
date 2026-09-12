-- 카테고리명 피그마 기준으로 변경
UPDATE category SET category_name = '주식·ETF' WHERE category_code = 'STK';
UPDATE category SET category_name = '세금·절세계좌' WHERE category_code = 'TAX';