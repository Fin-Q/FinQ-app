CREATE TABLE advanced_quiz_info (
    advanced_quiz_info_id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    intro_title VARCHAR(200) NOT NULL,
    intro_description TEXT NOT NULL,
    completion_title VARCHAR(200) NOT NULL,
    completion_description TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_advanced_quiz_info PRIMARY KEY (advanced_quiz_info_id),
    CONSTRAINT uk_advanced_quiz_info_category UNIQUE (category_id),
    CONSTRAINT fk_advanced_quiz_info_category FOREIGN KEY (category_id) REFERENCES category (category_id)
);

INSERT INTO advanced_quiz_info (category_id, intro_title, intro_description, completion_title, completion_description) VALUES
(1, '월급관리·저축, 얼마나 이해했을까요?',
 '지금까지 배운 내용을 바탕으로 서로 다른 개념을 함께 생각해보는 3개의 문제를 풀어볼게요.\\n\\n틀린 문제는 해설을 확인하고 다시 도전할 수 있어요.\\n\\n3문제를 모두 맞히면 심화퀴즈 완료!',
 '월급관리·저축 심화퀴즈 완료!',
 '이제 다음 내용을 설명할 수 있는지 확인해보세요.\\n\\n- 들어오는 돈과 나가는 돈의 흐름\\n- 고정비와 변동비의 차이\\n- 비상자금에서 유동성이 중요한 이유\\n- 예금과 적금의 납입 방식 차이\\n- 목적과 사용 시점에 따라 돈을 나눠 관리하는 이유'),
(2, '투자기초, 얼마나 이해했을까요?',
 '지금까지 배운 내용을 바탕으로 여러 개념을 함께 생각해보는 3개의 문제를 풀어볼게요.\\n\\n이번 심화퀴즈에서는\\n위험과 수익 / 복리 / 투자 비용 / 분산투자 / 적립식 투자를 종합적으로 확인해요.\\n\\n틀린 문제는 해설을 확인하고 다시 도전할 수 있어요.\\n\\n3문제를 모두 맞히면 심화퀴즈 완료!',
 '투자기초 심화퀴즈 완료!',
 '이제 다음 내용을 구분해서 설명할 수 있는지 확인해보세요.\\n\\n- 기대수익과 위험을 함께 봐야 하는 이유\\n- 시간이 복리에 영향을 주는 이유\\n- 투자 비용이 실제 결과에 영향을 줄 수 있는 이유\\n- 분산투자가 줄일 수 있는 위험과 줄이기 어려운 위험\\n- 적립식 투자와 분산투자의 차이'),
(3, '주식·ETF, 얼마나 이해했을까요?',
 '지금까지 배운 내용을 바탕으로 서로 다른 개념을 함께 생각하는 3문제를 풀어볼게요.\\n\\n확인 범위: 주식 / 지수 / ETF 구조 / 비용 / 이름 / 옵션 / ETF 분류\\n\\n3문제를 모두 맞히면 심화퀴즈 완료예요.',
 '주식·ETF 심화퀴즈 완료!',
 '이제 다음 내용을 구분해서 설명할 수 있는지 확인해보세요.\\n\\n- 주식과 ETF의 구조 차이\\n- 지수와 개별 종목의 차이\\n- ETF 비용이 발생하는 방식\\n- ETF 이름에서 읽을 수 있는 정보\\n- 액티브와 환헤지의 의미 차이\\n- 같은 지수를 추종해도 ETF가 달라질 수 있는 이유'),
(4, '세금·절세계좌, 얼마나 이해했을까요?',
 '지금까지 배운 내용을 바탕으로 여러 개념을 함께 생각하는 3문제를 풀어볼게요.\\n\\n확인 범위: 금융소득 / 주식·ETF 세금 / 절세계좌 / 과세이연 / ISA / 연금저축 / IRP\\n\\n3문제를 모두 맞히면 심화퀴즈 완료예요.',
 '세금·절세계좌 심화퀴즈 완료!',
 '이제 다음 내용을 구분해서 설명할 수 있는지 확인해보세요.\\n\\n- 금융소득과 원천징수\\n- 국내·해외주식의 세금 구조 차이\\n- ETF 유형과 상장 위치에 따른 과세 차이\\n- 비과세·세액공제·과세이연의 차이\\n- ISA·연금저축·IRP가 서로 다른 이유\\n- 세율·한도·조건은 최신 기준을 확인해야 한다는 점');