UPDATE advanced_quiz_info
SET intro_description = '지금까지 배운 내용을 바탕으로 서로 다른 개념을 함께 생각해보는 **3개의 문제**를 풀어볼게요.\\n\\n틀린 문제는 해설을 확인하고 다시 도전할 수 있어요.\\n\\n**3문제를 모두 맞히면 심화퀴즈 완료!**'
WHERE category_id = 1;

UPDATE advanced_quiz_info
SET intro_description = '지금까지 배운 내용을 바탕으로 여러 개념을 함께 생각해보는 **3개의 문제**를 풀어볼게요.\\n\\n이번 심화퀴즈에서는\\n**위험과 수익 / 복리 / 투자 비용 / 분산투자 / 적립식 투자**를 종합적으로 확인해요.\\n\\n틀린 문제는 해설을 확인하고 다시 도전할 수 있어요.\\n\\n**3문제를 모두 맞히면 심화퀴즈 완료!**'
WHERE category_id = 2;

UPDATE advanced_quiz_info
SET intro_description = '지금까지 배운 내용을 바탕으로 서로 다른 개념을 함께 생각하는 **3문제**를 풀어볼게요.\\n\\n확인 범위: **주식 / 지수 / ETF 구조 / 비용 / 이름 / 옵션 / ETF 분류**\\n\\n3문제를 모두 맞히면 심화퀴즈 완료예요.'
WHERE category_id = 3;

UPDATE advanced_quiz_info
SET intro_description = '지금까지 배운 내용을 바탕으로 여러 개념을 함께 생각하는 **3문제**를 풀어볼게요.\\n\\n확인 범위: **금융소득 / 주식·ETF 세금 / 절세계좌 / 과세이연 / ISA / 연금저축 / IRP**\\n\\n3문제를 모두 맞히면 심화퀴즈 완료예요.'
WHERE category_id = 4;