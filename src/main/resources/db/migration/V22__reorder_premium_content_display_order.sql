-- SAL: SAL-03(3) → SAL-P(4) → SAL-04(5) → SAL-05(6)
UPDATE content SET display_order = 6 WHERE content_code = 'SAL-05';
UPDATE content SET display_order = 5 WHERE content_code = 'SAL-04';
UPDATE content SET display_order = 4 WHERE content_code = 'SAL-P';

-- INV: INV-04(4) → INV-P(5) → INV-05(6)
UPDATE content SET display_order = 6 WHERE content_code = 'INV-05';
UPDATE content SET display_order = 5 WHERE content_code = 'INV-P';

-- ETF: ETF-04(4) → ETF-P(5) → ETF-05(6) → ETF-06(7) → ETF-07(8)
UPDATE content SET display_order = 8 WHERE content_code = 'ETF-07';
UPDATE content SET display_order = 7 WHERE content_code = 'ETF-06';
UPDATE content SET display_order = 6 WHERE content_code = 'ETF-05';
UPDATE content SET display_order = 5 WHERE content_code = 'ETF-P';

-- TAX: TAX-06(6) → TAX-P(7) → TAX-07(8) → TAX-08(9) → TAX-09(10)
UPDATE content SET display_order = 10 WHERE content_code = 'TAX-09';
UPDATE content SET display_order = 9 WHERE content_code = 'TAX-08';
UPDATE content SET display_order = 8 WHERE content_code = 'TAX-07';
UPDATE content SET display_order = 7 WHERE content_code = 'TAX-P';