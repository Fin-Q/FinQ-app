-- content_question 테이블에 after_page_order 컬럼 추가
ALTER TABLE content_question ADD COLUMN after_page_order INT NOT NULL DEFAULT 0;

-- 기존 데이터: 5페이지 구조 (P1→2페이지 뒤, P2→4페이지 뒤, F→5페이지 뒤)
-- 5페이지가 아닌 구조는 별도 설정
UPDATE content_question SET after_page_order = 2 WHERE content_stage = 'P1';
UPDATE content_question SET after_page_order = 4 WHERE content_stage = 'P2' AND question_code NOT IN ('SAL-04-P2', 'ETF-06-P2', 'TAX-03-P2', 'TAX-04-P2', 'TAX-07-P2', 'TAX-08-P2', 'TAX-09-P2');
UPDATE content_question SET after_page_order = 5 WHERE content_stage = 'F' AND question_code NOT IN ('SAL-04-F', 'ETF-02-F', 'ETF-06-F', 'TAX-03-F', 'TAX-04-F', 'TAX-07-F', 'TAX-08-F', 'TAX-09-F');

-- SAL-04: 6페이지 구조
UPDATE content_question SET after_page_order = 5 WHERE question_code = 'SAL-04-P2';
UPDATE content_question SET after_page_order = 6 WHERE question_code = 'SAL-04-F';

-- ETF-02: 6페이지 구조
UPDATE content_question SET after_page_order = 4 WHERE question_code = 'ETF-02-P2';
UPDATE content_question SET after_page_order = 6 WHERE question_code = 'ETF-02-F';

-- ETF-06: 6페이지 구조
UPDATE content_question SET after_page_order = 5 WHERE question_code = 'ETF-06-P2';
UPDATE content_question SET after_page_order = 6 WHERE question_code = 'ETF-06-F';

-- TAX-03: 6페이지 구조
UPDATE content_question SET after_page_order = 5 WHERE question_code = 'TAX-03-P2';
UPDATE content_question SET after_page_order = 6 WHERE question_code = 'TAX-03-F';

-- TAX-04: 6페이지 구조
UPDATE content_question SET after_page_order = 5 WHERE question_code = 'TAX-04-P2';
UPDATE content_question SET after_page_order = 6 WHERE question_code = 'TAX-04-F';

-- TAX-07: 6페이지 구조
UPDATE content_question SET after_page_order = 5 WHERE question_code = 'TAX-07-P2';
UPDATE content_question SET after_page_order = 6 WHERE question_code = 'TAX-07-F';

-- TAX-08: 6페이지 구조
UPDATE content_question SET after_page_order = 5 WHERE question_code = 'TAX-08-P2';
UPDATE content_question SET after_page_order = 6 WHERE question_code = 'TAX-08-F';

-- TAX-09: 7페이지 구조
UPDATE content_question SET after_page_order = 6 WHERE question_code = 'TAX-09-P2';
UPDATE content_question SET after_page_order = 7 WHERE question_code = 'TAX-09-F';

-- SAL-01: 현금흐름
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '월급은 들어왔는데 왜 매달 남는 돈이 없을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '월급이 얼마인지는 바로 떠오르는데, 지난달에 어디에 얼마를 썼는지는 생각보다 잘 모를 수 있어요. \\n 돈이 왜 남지 않는지 알고 싶다면 먼저 **들어온 돈과 나간 돈의 흐름**부터 확인해야 해요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '현금흐름',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '돈이 들어오고 나가는 흐름을 **현금흐름**이라고 해요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '들어오는 돈 → **수입**\\n나가는 돈 → **지출**\\n남는 돈 → **수입 - 지출**')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '월급이 같더라도 지출이 달라지면 매달 남는 돈도 달라질 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '지출을 나눠서 보기',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '지출을 비슷한 항목끼리 나눠보면 **내 돈이 어디로 나가고 있는지** 확인하기 쉬워져요.\\n\\n예를 들어 주거 / 통신·보험 / 교통 / 식비 / 카페·간식 / 쇼핑 / 여가·모임 처럼 묶어볼 수 있어요.\\n\\n월급이 **250만 원**인 가상의 직장인을 볼게요.'),
      JSON_OBJECT('type', 'IMAGE', 'imageUrl', 'https://finq-assets.s3.ap-northeast-2.amazonaws.com/img/SAL-01-3.png'),
      JSON_OBJECT('type', 'CAPTION', 'text', '※ 이해를 돕기 위한 가상 사례입니다.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '사례 적용',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '월급이 250만 원이어도\\n얼마를 지출했느냐에 따라 남는 돈은 달라져요.\\n\\n그래서 월급 금액만 보는 것보다\\n**수입과 지출을 함께 보는 것**이 중요해요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 돈을 관리할 때는 버는 돈과 쓰는 돈을 함께 봐야 해요.\\n- 들어온 돈과 나간 돈을 보면 한 달의 돈 흐름을 알 수 있어요.\\n- 현금흐름은 들어오는 돈과 나가는 돈의 흐름이에요.\\n- **월급이 같아도 지출이 달라지면 남는 돈은 달라질 수 있어요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'SAL-01';

-- SAL-02: 고정비·변동비
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '돈을 아끼려면 커피값부터 줄여야 할까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '지출을 줄이려고 하면 눈에 잘 띄는 소비부터 떠올리기 쉬워요. \\n 하지만 지출은 성격에 따라 나눠보면 더 쉽게 관리할 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '고정비와 변동비',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '매달 비교적 일정하게 반복되는 지출을 **고정비**, 사용량이나 선택에 따라 금액이 달라지는 지출을 **변동비**라고 해요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '고정비 → 월세, 정기 구독료처럼 반복되는 지출\\n변동비 → 식비, 카페, 쇼핑처럼 달마다 달라질 수 있는 지출')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '고정비라고 해서 매달 금액이 반드시 완전히 같다는 뜻은 아니에요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '관리 방식의 차이',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '고정비와 변동비는 지출되는 방식이 다르기 때문에 관리 방법도 달라질 수 있어요. \\n'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '예를 들어 고정비는 요금제나 계약 조건을 바꾸면 이후 지출에도 영향을 줄 수 있어요. \\n반면 변동비는 사용량이나 소비 횟수를 조절하면서 관리할 수 있어요. \\n')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '중요한 건 어느 한쪽부터 무조건 줄이는 게 아니라, 내 지출에서 어떤 항목이 크고 조절 가능한지 보는 것이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '통신비와 식비를 비교해보면',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '매달 통신비가 7만 원으로 비슷하게 나가고 식비는 어떤 달에는 30만 원, 어떤 달에는 50만 원이 나온다고 해볼게요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '통신비는 비교적 일정하게 반복되고 식비는 생활 방식이나 소비 횟수에 따라 달라질 수 있어요. \\n이렇게 지출의 성격을 나눠보면 어떤 방식으로 관리할지 판단하기 쉬워져요.')
      ))
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 지출을 관리할 때는 작은 소비부터 무조건 줄일 필요는 없어요.\\n- 먼저 **고정비와 변동비를 구분해 지출 구조를 보는 것**이 중요해요.\\n- 고정비는 비교적 일정하게 반복되는 지출이에요.\\n- 변동비는 사용량이나 선택에 따라 달라질 수 있는 지출이에요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'SAL-02';

-- SAL-03: 비상자금·유동성
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '모아둔 돈, 전부 투자해도 괜찮을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '투자할 돈이 많을수록 수익을 낼 기회도 커질 수 있어요. \\n하지만 갑자기 병원비가 생기거나, 예상하지 못한 지출이 필요해진다면 어떨까요? \\n이럴 때 바로 사용할 수 있도록 따로 준비해두는 돈이 **비상자금**이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '비상자금과 유동성',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '비상자금에서 중요한 건 단순히 돈이 있다는 것만이 아니에요. **필요할 때 얼마나 빠르고 쉽게 사용할 수 있는지**도 중요해요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '이처럼 자산을 현금으로 바꿔 사용할 수 있는 정도를 **유동성**이라고 해요.\\n갑자기 돈이 필요할수록 현금으로 바꾸기 쉽고 바로 사용할 수 있는 자금이 유리해요.')
      ))
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '자산마다 유동성이 달라요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '모든 자산을 똑같이 바로 사용할 수 있는 건 아니에요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '예를 들어 현금이나 바로 출금할 수 있는 예금은 비교적 사용하기 쉬워요.\\n반면 가격이 변하는 투자자산은 급하게 현금이 필요할 때 원하지 않는 시점에 팔아야 할 수도 있어요.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '그래서 자금을 볼 때는 **수익 가능성뿐 아니라 언제 사용할 돈인지**도 함께 봐야 해요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '비상자금과 투자금을 나눠보면',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '예상하지 못한 병원비처럼 **언제 필요할지 모르는 돈**과 당장 사용할 계획 없이 **장기간 운용하려는 돈**은 목적이 달라요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('title', '비상자금', 'text', '비상자금은 필요할 때 사용할 수 있는지가 중요하고,'),
        JSON_OBJECT('title', '투자금', 'text', '투자금은 손실 가능성과 운용 기간 등을 함께 고려해야 해요.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '즉, 모아둔 돈이라고 해서 모두 같은 방식으로 관리할 필요는 없어요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 비상자금은 예상하지 못한 상황에 대비하기 위한 돈이에요.\\n- 비상자금은 필요한 순간에 바로 사용할 수 있어야 해요.\\n- 유동성은 필요할 때 현금으로 사용하기 쉬운 정도예요.\\n- 자금의 목적과 사용할 시점에 따라 관리 방식은 달라질 수 있어요')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'SAL-03';

-- SAL-04: 예금·적금
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '금리가 똑같은데 왜 적금 이자는 더 적을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '예금과 적금에 같은 연 3%가 적혀 있다면 \\n받는 이자도 같을 것처럼 보일 수 있어요. \\n하지만 실제 이자는 **금리만으로 결정되지 않아요.** \\n돈을 언제, 얼마나 맡겼는지도 함께 봐야 해요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '예금과 적금의 차이',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '**예금**은 목돈을 한 번에 맡기는 방식이에요. 반면 **적금**은 일정 기간 동안 돈을 나눠서 넣는 방식이에요. \\n예를 들어 1,200만 원을 모은다고 하면,'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '**예금:** 1,200만 원을 처음부터 맡김\\n**적금:** 매달 100만 원씩 나눠 넣음')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '같은 금리라도 **돈이 들어가는 시점이 다르죠.**')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '이자를 받는 기간이 달라요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '예금은 처음 맡긴 목돈 전체가 일정 기간 동안 이자를 받아요.\\n하지만 적금은 매달 새로 돈을 넣기 때문에 각 납입금이 이자를 받는 기간이 달라요.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '첫 달에 넣은 돈은 오래 맡겨져 있지만, \\n마지막 달에 넣은 돈은 짧은 기간만 맡겨져 있는 식이에요. \\n그래서 **표시된 금리가 같아도 실제 받는 이자는 달라질 수 있어요.**')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '1,200만 원으로 비교하면',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '1년 동안 총 1,200만 원을 넣는다고 해볼게요.'),
      JSON_OBJECT('type', 'IMAGE', 'imageUrl', 'https://finq-assets.s3.ap-northeast-2.amazonaws.com/img/SAL-04-4.png'),
      JSON_OBJECT('type', 'TEXT', 'text', '즉, 최종적으로 넣은 원금이 같더라도 **각 돈이 맡겨져 있던 기간이 다르기 때문에 이자도 달라질 수 있어요.**'),
      JSON_OBJECT('type', 'CAPTION', 'text', '※ 이해를 돕기 위한 단순화된 사례입니다.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '금리만 비교하면 안 되는 이유',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '예금과 적금을 비교할 때는 \\n표시된 금리 숫자 하나만 보고 같은 상품처럼 생각하면 안 돼요. \\n**납입 방식과 돈이 맡겨지는 기간이 다르기 때문이에요.** \\n따라서 같은 금리라도 실제 이자 결과는 달라질 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 6,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 예금은 목돈을 한 번에 맡기는 방식이에요.\\n- 적금은 일정 기간 나눠서 납입하는 방식이에요.\\n- 같은 금리라도 **돈이 맡겨진 기간에 따라 실제 이자는 달라질 수 있어요.**\\n- 금리뿐 아니라 돈을 어떻게, 언제 넣는 상품인지도 함께 확인해야 해요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'SAL-04';

-- SAL-05: 목적별 자금관리
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '1년 뒤 쓸 돈도 수익률 높은 곳에 넣는 게 좋을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '돈을 관리할 때 수익률이 높을수록 무조건 좋다고 생각하기 쉬워요. \\n하지만 1년 뒤 꼭 써야 하는 돈과 당장 사용할 계획이 없는 돈을 똑같이 관리해도 될까요? \\n돈은 **언제, 무엇을 위해 사용할지**에 따라 관리 방식이 달라질 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '사용할 시점을 먼저 보기',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '자금을 관리할 때는 먼저 \\n**언제 사용할 돈인지**를 생각해볼 수 있어요. \\n예를 들면,'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 가까운 시기에 사용할 돈\\n- 몇 년 뒤 사용할 돈\\n- 사용 시점이 아직 정해지지 않은 돈')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '처럼 목적과 시점이 서로 다를 수 있어요. \\n사용할 시점이 가까울수록 \\n필요한 순간에 돈을 사용할 수 있는지가 중요해져요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '목적이 다르면 고려할 것도 달라요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 가까운 시기에 꼭 써야 하는 돈이라면 수익 가능성만큼 **손실 가능성과 유동성**도 중요해요.\\n- 반대로 당장 사용할 계획이 없는 자금은 더 긴 기간을 고려할 수 있겠죠.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '즉, 모든 돈에 똑같은 기준을 적용하기보다 **목적과 사용 시점에 맞춰 따로 보는 것**이 핵심이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '1년 뒤 결혼자금이라면',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '예를 들어 1년 뒤 결혼식에 사용할 돈이 있다고 해볼게요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 이 돈의 가장 중요한 목적은 **1년 뒤 필요한 시점에 사용할 수 있는 것**이에요.\\n- 최근 수익률이 높은지가 가장 먼저 볼 기준은 아니에요.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '언제 필요한 돈인지, 그때 필요한 금액을 사용할 수 있는지를 먼저 확인해야 해요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 돈은 모두 같은 목적을 가진 게 아니에요.\\n- 자금을 관리할 때는 **목적과 사용할 시점**을 먼저 확인해야 해요.\\n- 그다음 목적에 맞는 방식으로 관리할 수 있는지 살펴봐야 해요.\\n- 목적과 사용 시점에 따라 자금을 나눠 관리하는 것이 목적별 자금관리의 핵심이에요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'SAL-05';

-- SAL-P: 월급관리·저축습관 (프리미엄)
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '돈이 모이는 사람들은 월급날 뭐가 다를까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '같은 월급을 받아도 돈을 나누는 순서와 남겨두는 방식은 달라요. 월급이 들어온 뒤 소비와 저축을 어떻게 배분하는지 살펴봐요.')
    )
  )
),
updated_at = NOW()
WHERE content_code = 'SAL-P';

-- INV-01: 위험수익
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '수익률 10%짜리가 3%짜리보다 무조건 좋은 걸까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '투자할 때는 보통 수익률부터 눈에 들어와요. \\n하지만 기대할 수 있는 수익이 크다고 해서 그 투자 자체가 무조건 더 좋다고 볼 수는 없어요. \\n투자에서는 **수익 가능성과 함께 감수해야 하는 위험**도 봐야 해요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '위험은 무엇일까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '투자에서 위험은 단순히 "돈을 잃는다"는 뜻만은 아니에요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 원금보다 적어질 가능성\\n- 가격이 크게 오르내리는 정도')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '처럼 여러 모습으로 나타날 수 있어요. \\n가격이 얼마나 크게 흔들리는지를 **변동성**이라고도 해요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '수익과 위험을 함께 보기',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 기대수익이 높은 투자일수록 더 큰 가격 변동이나 손실 가능성을 함께 가질 수 있어요.\\n- 반대로 가격 변화가 상대적으로 작은 자산은 기대할 수 있는 수익도 다를 수 있고요.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '중요한 건 **수익률 숫자 하나만 보고 판단하지 않는 것**이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '같은 100만 원이라도',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '100만 원을 1년 동안 운용한다고 해볼게요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('title', 'A', 'text', '결과 변동이 비교적 작음'),
        JSON_OBJECT('title', 'B', 'text', '더 큰 수익을 얻을 수도 있지만 손실 폭도 커질 수 있음')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '한쪽은 결과 변동이 비교적 작고, 다른 쪽은 더 큰 수익을 얻을 수도 있지만 손실 폭도 커질 수 있어요. \\n따라서 "기대수익률이 더 높다"는 정보만으로는 어느 쪽이 더 적절한지 판단하기 어려워요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 투자에서는 **수익 가능성과 위험을 함께 보는 것**이 중요해요.\\n- 수익은 투자로 얻을 수 있는 결과예요.\\n- 위험은 손실 가능성이나 가격 변동을 뜻해요.\\n- 기대수익률이 높다는 이유만으로 더 좋은 투자라고 단정할 수는 없어요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'INV-01';

-- INV-02: 복리
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '같은 돈을 모아도 일찍 시작하면 얼마나 달라질까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '같은 금액을 같은 수익률로 운용하더라도 기간이 길어지면 결과가 달라질 수 있어요. \\n그 차이를 만드는 개념 중 하나가 **복리**예요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '단리와 복리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('title', '단리', 'text', '처음 넣은 원금을 기준으로 수익이 붙는 방식'),
        JSON_OBJECT('title', '복리', 'text', '이전에 발생한 수익까지 다시 계산의 기반이 될 수 있음.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '단리는 처음 넣은 원금을 기준으로 수익이 붙는 방식이에요. 반면 복리는 이전에 발생한 수익까지 다시 계산의 기반이 될 수 있어요. \\n쉽게 말하면 **원금에 수익 → 그 수익이 더해진 금액에 다시 수익**이 이어지는 구조예요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '시간이 길어질수록',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '복리는 처음에는 단리와 차이가 크게 느껴지지 않을 수 있어요. \\n하지만 시간이 지나면서 이전에 쌓인 수익에도 다시 수익이 붙을 기회가 늘어나요. \\n그래서 다른 조건이 같다면 **운용 기간이 길어질수록 복리 효과가 커질 수 있어요.**')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '10년과 30년',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'IMAGE', 'imageUrl', 'https://finq-assets.s3.ap-northeast-2.amazonaws.com/img/INV-02-4.png'),
      JSON_OBJECT('type', 'TEXT', 'text', '예시는 1,000만 원을 연 6%로 운용한다고 가정했을 때 10년과 30년 결과를 비교해 복리의 기간 효과를 보여줘요. \\n핵심은 정확한 숫자를 외우는 게 아니에요. \\n**같은 원금과 같은 수익률이어도 기간이 길어지면 단리와 복리의 결과 차이가 더 커질 수 있다**는 점이에요.'),
      JSON_OBJECT('type', 'CAPTION', 'text', '※ 이해를 돕기 위한 가정이며 실제 수익을 보장하지 않습니다.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 단리는 주로 처음 원금을 기준으로 수익을 계산해요.\\n- 복리는 **누적된 수익도 다시 수익 계산의 기반이 될 수 있어요.**\\n- 기간이 길어질수록 복리 효과는 더 커질 수 있어요.\\n- 복리는 시간과 함께 작동하는 구조라고 이해하면 돼요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'INV-02';

-- INV-03: 투자비용
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '수수료 1% 차이, 정말 신경 쓸 만큼 클까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '투자를 볼 때 수익률은 열심히 비교하면서 비용은 작아 보여서 지나치기 쉬워요. \\n하지만 비용도 결국 **내가 실제로 얻는 결과에서 빠지는 금액**이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '투자에는 비용이 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '투자 비용은 크게 두 가지가 있어요'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('title', '거래 비용', 'text', '사고팔 때 발생하는 비용 ( 수수료, 세금 등 )'),
        JSON_OBJECT('title', '운용 보수', 'text', '상품을 보유하는 동안 반복해서 발생할 수 있는 비용')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '특히 반복되는 비용은 기간이 길어질수록 누적될 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '수익률이 같아도 결과는 달라질 수 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '두 투자의 투자 전 성과가 같더라도 \\n부담하는 비용이 다르면 실제 남는 결과는 달라질 수 있어요. \\n쉽게 보면, \\n**투자로 얻은 결과 − 비용 = 실제로 남는 결과** \\n라고 생각할 수 있어요. \\n비용은 작아 보여도 계속 반복되면 차이가 쌓일 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '장기일수록 차이가 쌓여요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'IMAGE', 'imageUrl', 'https://finq-assets.s3.ap-northeast-2.amazonaws.com/img/INV-03-4.png'),
      JSON_OBJECT('type', 'TEXT', 'text', '예시에서는 같은 1,000만 원, 같은 연 7% 수익률을 가정하고 비용만 다르게 했을 때 20년 뒤 결과가 달라지는 사례를 제시해요. \\n중요한 건 숫자 자체보다 **같은 성과라도 반복되는 비용 차이가 장기적으로 누적될 수 있다는 것**이에요.'),
      JSON_OBJECT('type', 'CAPTION', 'text', '※ 계산 예시는 공개 전 다시 검산해야 합니다.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 투자 결과를 볼 때는 **수익률과 비용을 함께 확인해야 해요.**\\n- 매매 수수료는 거래 과정에서 발생하는 비용이에요.\\n- 운용 보수는 보유 기간 동안 반복해서 발생할 수 있어요.\\n- 작은 비용도 장기간 반복되면 투자 결과에 영향을 줄 수 있어요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'INV-03';

-- INV-04: 분산투자
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '주식 하나만 잘 고르면 분산투자는 필요 없을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '한 곳에 투자했는데 그 대상에 문제가 생기면 전체 투자 결과가 크게 영향을 받을 수 있어요. \\n이런 집중된 위험을 나누기 위해 사용하는 방법이 **분산투자**예요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '무엇을 나눌 수 있을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '분산은 한 가지 방식만 있는 게 아니에요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 종목 나누기 → 여러 회사에 나누기\\n- 자산군 나누기 → 주식·채권·현금 등으로 나누기\\n- 지역 나누기 → 여러 지역으로 나누기')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '핵심은 **한 대상에 지나치게 집중하지 않는 것**이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '줄일 수 있는 위험',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 특정 회사의 실적 악화나 경영 문제처럼 한 대상에만 생기는 위험을 **개별 위험**이라고 볼 수 있어요.\\n- 여러 대상에 나눠 투자하면 한 곳의 문제가 전체에 미치는 영향을 줄이는 데 도움이 될 수 있어요.')
      ))
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '분산으로도 못 막는 위험',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '반대로 시장 전체가 함께 하락하는 상황도 있어요. \\n이런 **시장 위험**은 여러 종목으로 나눠 투자했다고 해서 사라지지 않아요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('title', '그래서 분산투자는', 'text', '**모든 손실을 막는 방법이 아니라 특정 대상에 집중된 위험을 줄이는 방법**으로 이해해야 해요.')
      ))
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 분산투자는 여러 곳에 나누어 투자하는 방식이에요.\\n- 개별 위험은 분산을 통해 영향을 줄이는 데 도움이 될 수 있어요.\\n- 시장 위험은 분산해도 남을 수 있어요.\\n- **분산투자는 위험을 없애는 것이 아니라 집중된 위험을 나누는 방식이에요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'INV-04';

-- INV-05: 적립투자
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '지금 사면 고점일까 봐 무섭다면 어떻게 해야 할까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '투자를 시작하려고 하면 "지금 사도 될까?"라는 고민이 생길 수 있어요. \\n하지만 미래의 가격이 언제 가장 높거나 낮을지는 미리 알기 어려워요. \\n이런 **투자 시점의 부담을 나누는 방식** 중 하나가 적립식 투자예요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '적립식 투자란?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '**적립식 투자**는 정해진 간격으로 일정한 금액을 나누어 투자하는 방식이에요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '**가격이 낮을 때:** 같은 돈으로 더 많은 수량 \\n\\n**가격이 높을 때:** 같은 돈으로 더 적은 수량')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '예를 들어 같은 금액을 정기적으로 투자하면 가격이 낮을 때는 같은 돈으로 더 많은 수량을, 가격이 높을 때는 같은 돈으로 더 적은 수량을 사게 될 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '분산투자와는 달라요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '분산투자와 적립식 투자는 나누는 대상이 달라요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- **분산투자** → 무엇을 살지 나눔\\n- **적립식 투자** → 언제 살지 나눔')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '그래서 둘은 같은 개념이 아니에요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '적립식에도 한계가 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '적립식 투자가 항상 더 높은 수익을 만들어주는 것은 아니에요. \\n예를 들어 가격이 계속 상승하는 상황에서는 \\n처음에 한 번에 투자한 경우가 결과적으로 더 나았을 수도 있어요. \\n적립식 투자의 핵심은 **수익을 보장하는 것이 아니라 투자 시점을 나눠 위험을 줄이는 것**이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 적립식 투자는 일정한 간격으로 자금을 나누어 투자하는 방식이에요.\\n- 가격에 따라 같은 금액으로 살 수 있는 수량은 달라질 수 있어요.\\n- 분산투자와는 나누는 대상이 달라요.\\n- **적립식 투자는 투자 시점을 나누는 방식이며, 더 높은 수익을 보장하지는 않아요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'INV-05';

-- INV-P: 투자심리·투자원칙 (프리미엄)
UPDATE content SET body_data = JSON_ARRAY(
        JSON_OBJECT(
                'order', 1,
                'title', '왜 남들은 오를 때 잘 사고, 나는 꼭 늦은 것 같을까요?',
                'content', JSON_ARRAY(
                        JSON_OBJECT('type', 'TEXT', 'text', '투자에서는 가격보다 판단이 흔들리는 순간이 더 중요할 수 있어요. 자주 빠지는 생각의 함정을 알아봐요.')
                           )
        )
                               ),
                   updated_at = NOW()
WHERE content_code = 'INV-P';

-- ETF-01 : 주식기초
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '주식 1주를 사면 나는 정확히 뭘 갖게 되는 걸까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '주식을 산다는 건 단순히 가격이 오르내리는 숫자를 사는 게 아니에요. \\n주식은 **회사의 소유권을 잘게 나눈 조각**이에요. \\n주식 1주를 가진다는 건 그 회사의 아주 작은 일부를 소유한다는 뜻이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '주주가 된다는 것',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '주식을 가진 사람을 **주주**라고 해요. \\n주주는 회사의 소유권 일부를 가진 사람이에요. \\n회사가 성장하거나 이익을 내면 주주는 그 결과와 연결될 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '주식에서 생길 수 있는 경제적 결과',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '주식에서는 대표적으로 두 가지 경제적 결과가 생길 수 있어요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 회사가 이익 일부를 나눠주면 → **배당**\\n- 산 가격보다 높은 가격에 팔면 → **시세차익**')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '반대로 주가가 내려가면 손실이 생길 수도 있어요. \\n그리고 **모든 회사가 반드시 배당을 지급하는 것은 아니에요.**')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '간단한 사례',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'IMAGE', 'imageUrl', ''),
      JSON_OBJECT('type', 'TEXT', 'text', '예를 들어 주식을 5만 원에 샀는데 \\n나중에 7만 원이 되어 팔았다면 2만 원의 시세차익이 생겨요. 반대로 4만 원에 팔았다면 손실이 발생하겠죠. \\n즉 주식을 가진다는 건 회사의 가치 변화와 내 자산의 가치가 연결된다는 뜻이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 주식은 회사의 소유권 일부를 의미해요.\\n- 주주는 주식을 보유한 사람이에요.\\n- 배당은 회사가 이익 일부를 주주에게 나눠주는 것이에요.\\n- 시세차익은 산 가격보다 높은 가격에 팔아 생긴 차익이에요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'ETF-01';

-- ETF-02: 주가지수
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '코스피가 올랐다는 건 내 주식도 올랐다는 뜻일까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '뉴스에서는 "오늘 코스피가 올랐다" 같은 말을 자주 들어요. \\n하지만 코스피가 올랐다고 해서 내가 가진 모든 주식이 반드시 오른 건 아니에요. \\n왜냐하면 **지수는 여러 종목의 주가를 묶어서 보여주는 숫자**이기 때문이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '지수란?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('title', '지수(인덱스)', 'text', '여러 종목을 묶어서 시장이나 특정 그룹의 흐름을 하나의 숫자로 보여주는 지표')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '쉽게 말하면 **여러 회사의 성적을 한눈에 보는 성적표**처럼 볼 수 있어요. \\n특정 회사 하나의 주가를 보여주는 숫자가 아니에요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '모든 종목이 똑같이 움직이진 않아요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '지수 안에는 여러 종목이 들어 있어요. \\n어떤 종목은 오르고, 어떤 종목은 내릴 수도 있어요. \\n그 결과를 종합해 지수가 움직이기 때문에 \\n**지수 상승 = 모든 종목 상승**은 아니에요. \\n또 지수마다 종목을 반영하는 방식도 다를 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '코스피와 코스피200',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('title', '코스피', 'text', '유가증권시장 전체 흐름을 보여주는 대표 지수'),
        JSON_OBJECT('title', '코스피200', 'text', '유가증권시장을 대표하는 200개 종목을 선정해 만든 지수')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '그래서 이름이 비슷해도 같은 지수는 아니에요. \\nETF를 보다 보면 코스피 자체보다 코스피200을 따라가는 상품을 자주 보게 돼요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '자주 만나게 되는 지수 예시',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('title', '코스피200', 'text', '한국 유가증권시장을 대표하는 200개 종목으로 구성된 지수.'),
        JSON_OBJECT('title', '코스닥150', 'text', '코스닥시장을 대표하는 150개 종목으로 구성된 지수.'),
        JSON_OBJECT('title', 'S&P500', 'text', '미국의 대표적인 대형 기업 500개로 구성된 지수.'),
        JSON_OBJECT('title', '나스닥100', 'text', '나스닥에 상장된 대표적인 대형 비금융기업 100개로 구성된 지수. 기술기업 비중이 높은 편.')
      ))
    )
  ),
  JSON_OBJECT(
    'order', 6,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 지수는 여러 종목의 흐름을 한눈에 보여주는 지표예요.\\n- 지수가 올라도 모든 종목이 함께 오르는 것은 아니에요.\\n- 지수마다 포함 종목과 구성 방식이 달라요.\\n- **ETF를 볼 때는 어떤 지수를 따라가는지 확인해야 해요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'ETF-02';

-- ETF-03: ETF 기초
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', 'ETF 하나만 사도 여러 회사에 투자한 게 된다고요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '주식 한 종목은 특정 회사 하나에 투자하는 거예요. \\n반면 ETF는 여러 자산을 하나의 상품 안에 담을 수 있어요. \\n그래서 흔히 **여러 자산을 담은 바구니**에 비유해요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', 'ETF란?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF의 정식 이름은 **상장지수펀드**예요. 여러 주식이나 채권 등을 한 상품에 담고, 그 상품을 거래소에서 주식처럼 사고팔 수 있게 만든 구조예요. \\n즉, **여러 자산을 담은 펀드 + 거래소에서 매매 가능**\\n이라고 이해하면 쉬워요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '개별주식과 다른 점',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '개별주식은 한 기업의 소유권 일부를 사는 거예요. \\nETF는 여러 자산을 정해진 기준에 따라 한 상품으로 구성할 수 있어요. \\n그래서 한 종목에만 투자하는 것과는 구조가 달라요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '과일 바구니처럼 생각해보기',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '과일을 산다고 해볼게요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- **사과만 한 봉지:** 한 종류만 선택\\n- **여러 과일이 담긴 세트:** 여러 종류를 한 번에 선택')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF도 이와 비슷하게 여러 자산이 담긴 상품을 한 번에 거래하는 구조예요. \\n다만 **모든 ETF가 똑같이 넓게 분산된 것은 아니에요.**\\n구성 자산에 따라 집중도가 다를 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- ETF는 여러 자산으로 구성될 수 있는 펀드예요.\\n- 거래소에서 주식처럼 사고팔 수 있어요.\\n- ETF는 개별주식과 구성 방식이 달라요.\\n- **ETF라고 해서 모두 같은 수준의 분산 효과를 갖는 것은 아니에요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'ETF-03';

-- ETF-04: ETF 비용
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '낸 기억도 없는데 ETF 수수료는 언제 빠져나갈까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF를 보유하면 운용에 필요한 비용이 발생할 수 있어요. \\n그런데 카드값처럼 "수수료가 빠져나갔다"는 알림을 받는 방식은 아닐 수 있어요. \\n그래서 비용을 내고 있다는 사실을 체감하기 어려워요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '총보수란?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF를 운용하는 데 드는 비용 중 대표적으로 확인하는 지표가 **총보수**예요. \\n이 비용은 ETF를 보유하는 동안 ETF 자산에서 차감되어 기준가격에 반영돼요. \\n그래서 따로 송금하거나 결제하는 비용처럼 느껴지지 않아요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '총보수만 보면 끝일까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '여기서 주의할 점이 있어요. \\nETF에는 총보수 외에도 기타 비용이 발생할 수 있어요. \\n그래서 실제 부담 비용을 확인할 때는 \\n**총보수 하나만으로 모든 비용을 판단하기 어려울 수 있어요.**')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '작은 차이도 누적될 수 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '비슷한 ETF라도 비용 수준은 다를 수 있어요. \\n매년 반복되는 비용은 기간이 길어질수록 누적될 수 있어요. \\n그래서 같은 조건이라면 비용 차이가 장기적인 결과에 영향을 줄 수 있어요. \\n그래서 꼭, 운용사 홈페이지나 공시자료를 참고해서 비용을 비교하시는게 좋아요. \\nETF의 비용을 비교할 때는 운용사 홈페이지나 공시자료에서 세부 비용을 함께 확인할 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- ETF를 보유하는 동안 운용 비용이 발생할 수 있어요.\\n- 총보수는 ETF의 대표적인 비용 지표예요.\\n- 실제 부담 비용은 총보수 외 요소도 함께 확인해야 해요.\\n- **반복되는 비용은 장기 투자 결과에 영향을 줄 수 있어요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'ETF-04';

-- ETF-P: ETF·분배금 (프리미엄)
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', 'ETF도 배당을 준다고요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF가 주는 배당을 분배금이라고 불러요. ETF가 무엇을 담고 있는지에 따라 분배금도 달라진답니다. 언제, 어떻게 나오는지 함께 알아봐요.')
    )
  )
),
updated_at = NOW()
WHERE content_code = 'ETF-P';

-- ETF-05: ETF 이름
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', 'TIGER, KODEX, ACE… 이름만 보고 차이를 알 수 있을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF 이름은 처음 보면 길고 복잡해 보여요. \\n하지만 이름을 몇 부분으로 나눠보면 어떤 상품인지 기본 구조를 읽을 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '이름은 몇 덩어리로 볼 수 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF 이름은 보통 이런 식으로 나눠볼 수 있어요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- **브랜드**\\n- **추종 대상**\\n- **추가 옵션**')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '예를 들면 브랜드 + S&P500 + (H) 처럼 볼 수 있어요. \\n각 부분이 서로 다른 정보를 알려줘요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '앞부분은 브랜드',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'TIGER, KODEX, ACE 같은 표현은 \\nETF를 만든 운용사를 구분하는 브랜드예요. \\n즉 브랜드는 **누가 만든 ETF인지**를 알려주는 단서예요. \\n브랜드가 같다고 해서 \\n모든 ETF의 구성이나 위험이 같은 건 아니에요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '가운데는 무엇을 따라가는지',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '이름 가운데에는 ETF가 어떤 지수나 자산을 따라가는지 나타내는 표현이 들어갈 수 있어요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '예를 들어\\n- S&P500\\n- 나스닥100\\n- 코스피200')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '같은 식이에요. \\n뒤에는 환헤지, 액티브, 혼합형 같은 추가 정보가 붙을 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- ETF 이름은 보통 **운용사 브랜드 → 추종 대상 → 추가 옵션** 순서로 볼 수 있어요.\\n- 이름을 보면 ETF가 무엇을 추종하는지 대략 파악할 수 있어요.\\n- 이름만으로 비용, 실제 구성, 위험까지 모두 알 수는 없어요.\\n- 자세한 내용은 운용사 홈페이지의 투자설명서를 확인하는 게 좋아요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'ETF-05';

-- ETF-06: ETF 옵션
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', 'ETF 이름 뒤의 (H)와 액티브는 무슨 뜻일까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF 이름 뒤에는 상품의 특징을 알려주는 표시가 붙을 수 있어요. \\n그중 자주 헷갈리는 게 **액티브**와 **(H)**예요. \\n둘은 전혀 다른 정보를 나타내요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '패시브와 액티브',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '**패시브 ETF:** 특정 지수나 기준을 따라가는 것을 주된 목표로 함. \\n\\n**액티브 ETF:** 운용자의 판단이나 전략을 더 적극적으로 활용할 수 있음.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '액티브라고 해서 항상 더 높은 수익을 낸다는 뜻은 아니에요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '환율도 결과에 영향을 줄 수 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '해외자산에 투자하면 \\n자산 가격뿐 아니라 **환율 변화**도 결과에 영향을 줄 수 있어요. \\n예를 들어 해외자산 가격이 그대로여도 \\n환율이 바뀌면 원화 기준 결과는 달라질 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '환헤지와 환노출',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '투자 비용은 크게 두 가지가 있어요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '**환헤지:** 환율 변화가 투자 결과에 미치는 영향을 줄이기 위한 방식 \\n**환노출:** 환율 변화의 영향을 그대로 받는 구조')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF 이름의 (H)는 일반적으로 환헤지와 관련된 표시로 사용돼요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '같은 이름 안에서도 다른 정보',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '정리하면'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- **액티브:** 운용 방식에 대한 정보\\n- **(H):** 환율 위험 관리 방식에 대한 정보')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '서로 같은 개념이 아니에요.')
    )
  ),
  JSON_OBJECT(
    'order', 6,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 패시브 ETF는 지수 등을 따라가는 방식이에요.\\n- 액티브 ETF는 운용 판단을 더 적극적으로 활용하는 방식이에요.\\n- 환헤지는 환율 변화의 영향을 줄이려는 방식이에요.\\n- 환노출은 환율 변화의 영향을 받을 수 있는 구조예요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'ETF-06';

-- ETF-07: ETF 종류
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '둘 다 S&P500인데 왜 실제 결과는 달라질 수 있을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '같은 지수를 따라가는 ETF라면 결과도 완전히 같을 것처럼 보일 수 있어요. \\n하지만 ETF는 **어디에 상장됐는지, 무엇을 담았는지, 어떤 구조인지**에 따라 차이가 생길 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '상장 위치가 다를 수 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF는 국내 거래소에 상장될 수도 있고 해외 거래소에 상장될 수도 있어요. \\n같은 S&P500을 따라가더라도 상장된 시장이 다르면 거래 환경이나 구조가 달라질 수 있어요. \\n즉 **추종 지수가 같다는 것만으로 같은 상품은 아니에요.**')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '담고 있는 자산도 다를 수 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF는 주식만 담는 상품만 있는 게 아니에요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '예를 들어\\n- 주식 중심\\n- 채권 포함\\n- 여러 자산 혼합')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '처럼 구성 방식이 다를 수 있어요. \\n이런 차이는 ETF의 움직임에도 영향을 줄 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '같은 지수를 따라가도 차이가 나는 이유',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '같은 지수를 추종하더라도'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 비용\\n- 상장 위치\\n- 환율 관련 구조\\n- 운용 방식')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '등이 다를 수 있어요. \\n그래서 실제 투자자가 경험하는 결과는 완전히 같지 않을 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- ETF를 비교할 때는 **추종 지수만 보는 것으로는 부족해요.**\\n- 상장 시장과 실제 구성 자산을 함께 확인해야 해요.\\n- 비용과 환율 관련 구조도 비교할 필요가 있어요.\\n- 세금 차이는 다음 TAX 챕터에서 이어서 알아봐요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'ETF-07';

-- TAX-01: 금융소득
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '이자나 배당으로 번 돈도 소득으로 잡힐까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '소득이라고 하면 보통 월급부터 떠올리기 쉬워요. \\n하지만 내가 직접 일해서 번 돈뿐 아니라 **돈이나 금융자산에서 생긴 수익도 소득이 될 수 있어요.**\\n이런 소득을 **금융소득**이라고 해요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '금융소득에는 무엇이 있을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '금융소득은 크게 두 가지로 볼 수 있어요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- **이자소득:** 예금·적금 등에 돈을 맡기고 받은 이자\\n- **배당소득:** 주식에서 받은 배당이나 ETF 분배금 등')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '즉 돈을 맡기거나 금융자산을 보유한 결과로 생긴 소득이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '세금이 먼저 빠질 수 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '이자나 배당을 받을 때, 세금은 내가 따로 내는 게 아니라 먼저 빠진 뒤 나머지가 입금돼요. \\n돈을 지급하는 금융회사가 세금을 먼저 떼고 지급하는 방식을 **원천징수**라고 해요. \\n그래서 화면에서 본 이자나 배당 금액과 실제로 계좌에 들어온 금액이 다를 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '생활 속에서 보면',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '예금에서 이자가 생겼다고 해볼게요. \\n앱에는 세금이 빠지기 전 이자가 표시될 수 있지만, \\n실제로 통장에 들어올 때는 세금이 반영된 금액이 들어와요. \\n즉 **발생한 소득과 실제 입금액은 같지 않을 수 있어요.**')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 금융소득은 금융자산에서 발생하는 소득이에요.\\n- 대표적으로 이자소득과 배당소득이 있어요.\\n- 금융소득은 지급 과정에서 원천징수가 이루어질 수 있어요.\\n- **발생한 소득과 실제 입금액은 같지 않을 수 있어요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'TAX-01';

-- TAX-02: 이자·배당소득세
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '이자 10만 원인데 왜 통장에는 10만 원이 안 들어올까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '금융상품에서 이자나 배당이 발생하면 표시된 금액이 그대로 들어오지 않아요. \\n그 이유는 **세금이 먼저 반영되기 때문**이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '세전과 세후',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '세금을 빼기 전 금액을 **세전**, 세금을 반영한 뒤 실제로 받는 금액을 **세후**라고 해요. \\n예를 들어 **세전 이자 → 세금 반영 → 세후 입금액** 의 순서로 생각하면 쉬워요. \\n일반적인 국내 이자·배당소득에는 \\n**소득세 14%와 지방소득세를 포함해 통상 15.4%가 원천징수돼요.** \\n다만 소득 종류나 조건에 따라 세율은 달라질 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '원천징수 다시 보기',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '앞에서 배운 원천징수는 돈을 지급하는 쪽이 세금을 먼저 떼어주는 방식이에요. \\n그래서 사용자가 직접 세금을 내지 않아도 이미 세금이 반영된 금액을 받을 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '금융소득이 커지면 달라질 수 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '금융소득이 일정 기준을 넘는 경우에는 과세 방식이 달라질 수 있어요. \\n이때 등장하는 개념이 **금융소득종합과세**예요. \\n여기서는 정확한 금액 기준보다 **금융소득 규모에 따라 과세 방식이 달라질 수 있다**는 점만 기억하면 돼요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 세전은 세금이 차감되기 전 금액이에요.\\n- 세후는 세금이 반영된 뒤 실제 받는 금액이에요.\\n- 이자·배당은 지급 과정에서 원천징수될 수 있어요.\\n- **금융소득 규모에 따라 과세 방식이 달라질 수 있어요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'TAX-02';

-- TAX-03: 주식세금
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '주식으로 똑같이 벌어도 세금은 다를 수 있을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '주식을 싸게 사서 비싸게 팔아 생긴 이익을 **매매차익**이라고 해요. \\n그런데 같은 매매차익이라도 국내주식인지 해외주식인지에 따라 세금 구조가 달라요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '먼저 시장을 구분해요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '주식 세금을 볼 때는 먼저 **어디에 상장된 주식인지**를 구분해야 해요. \\n국내주식과 해외주식은 매매차익에 적용되는 과세 방식이 서로 다르기 때문이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '국내주식',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '일반적인 소액주주가 장내에서 거래했다면 이 매매차익에는 양도소득세가 부과되지 않아요. \\n삼성전자를 5만 원에 사서 7만 원에 팔아 2만 원을 남겼다면, 이 차익은 **비과세**예요. \\n다만 대주주에 해당하거나 장외에서 거래하는 경우 등에는 과세될 수 있어요.'),
      JSON_OBJECT('type', 'CAPTION', 'text', '기준일 2026.8 · 출처 국세청')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '해외주식',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '일반적인 해외주식 양도차익에는 양도소득세 20%가 적용되고 지방소득세를 포함하면 통상 22%예요. \\n여기서 두 가지를 기억하세요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('title', '① 연간 주식 양도소득에 대해 250만 원의 기본공제가 적용돼요.', 'text', '1년간 해외주식으로 번 이익이 250만 원 이하면 세금은 0원이에요'),
        JSON_OBJECT('title', '② 직접 신고·납부해야 해요.', 'text', '국내 이자·배당처럼 세금이 자동으로 원천징수되는 방식이 아니라 다음 해 5월에 직접 신고·납부해야 해요.')
      ))
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '손익을 함께 보는 경우',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '해외주식 세금은 **이익과 손실을 합쳐서** 계산해요. 이를 **손익통산**이라고 해요. \\nA 종목에서 500만 원의 이익이 나고 B 종목에서 300만 원의 손실이 났다면 먼저 손익을 합쳐 200만 원으로 계산해요. \\n여기에 연 250만 원의 기본공제가 있어요. 합친 금액이 이 범위 안에 들어오는지에 따라 세금 계산이 달라져요. \\n즉 세금은 단순히 "얼마 벌었나"만 보는 게 아니라 어떤 시장에서 어떤 방식으로 거래했는지도 중요해요.')
    )
  ),
  JSON_OBJECT(
    'order', 6,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 매매차익은 주식을 사고팔아 생긴 이익이에요.\\n- 국내주식과 해외주식은 매매차익 과세 구조가 달라요.\\n- 해외주식은 공제·신고 등 별도 세금 기준을 확인해야 해요.\\n- 구체적인 세율과 기준은 최신 제도를 확인해야 해요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'TAX-03';

-- TAX-04: ETF 세금
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '같은 ETF처럼 보여도 세금은 다를 수 있을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ETF라고 해서 세금이 모두 같은 방식으로 적용되는 것은 아니에요. \\n**어디에 상장됐는지, 무엇을 담고 있는지**에 따라 \\n과세 구조가 달라질 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '국내주식형 ETF',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '**국내주식형 ETF**는 매매차익에 세금이 부과되지 않아요.\\n다만 ETF에서 지급되는 **분배금(배당금)에는 배당소득세가 부과될 수 있어요.**')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '그 외 국내상장 ETF',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '국내에 상장돼 있어도 **해외주식·채권·원자재 등을 담은 ETF는 국내주식형 ETF와 과세 방식이 달라요.**\\n매매 과정에서 발생한 이익에 대해 배당소득으로 과세될 수 있어요. \\n즉 **국내에 상장됐다는 이유만으로 모두 같은 세금 구조가 적용되는 것은 아니에요.**')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '해외상장 ETF',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '해외 증시에 상장된 ETF를 직접 거래하면 **해외주식과 같은 양도소득 과세 구조가 적용돼요.** \\n일정 금액의 기본공제가 적용되고 이를 초과한 양도차익에는 세금이 부과될 수 있어요. \\n그래서 같은 지수를 추종하더라도 국내상장 ETF와 해외상장 ETF의 세금 처리는 달라질 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '같은 S&P500이어도',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '예를 들어 둘 다 S&P500을 따라가더라도'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 국내에 상장됐는지\\n- 해외에 상장됐는지\\n- 이익이 얼마나 났는지')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '에 따라 과세 방식이 달라질 수 있어요. \\nETF는 이름이 비슷해도 상장 위치와 자산 구성에 따라 세금 구조는 달라질 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 6,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- ETF 세금은 **상장 위치와 자산 구성에 따라 달라질 수 있어요.**\\n- 국내주식형 ETF는 매매차익에 세금이 부과되지 않아요.\\n- 국내상장 ETF라도 해외주식·채권·원자재 등을 담으면 과세 방식이 달라질 수 있어요.\\n- 해외상장 ETF는 해외주식과 같은 양도소득 과세 구조가 적용돼요.')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'TAX-04';

-- TAX-05: 절세계좌
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '같은 투자를 해도 계좌만 바꾸면 세금이 달라질까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '같은 금융상품에 투자해도 \\n어떤 계좌에서 보유하느냐에 따라 세금이 달라질 수 있어요. \\n일반계좌와 다른 세제 혜택을 제공하도록 만들어진 계좌를 \\n보통 **절세계좌**라고 불러요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '어떤 혜택이 있을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '절세계좌의 혜택은 하나만 있는 게 아니에요. \\n대표적으로'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- **비과세** → 일정 조건에서 세금을 부과하지 않음\\n- **세액공제** → 내야 할 세금에서 일정 금액을 줄여줌\\n- **과세이연** → 세금 납부 시점을 뒤로 미룸')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '같은 방식이 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '계좌마다 혜택이 달라요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '모든 절세계좌가 \\n세 가지 혜택을 전부 제공하는 것은 아니에요. \\nISA, 연금저축, IRP 등은 \\n목적과 조건, 적용되는 세제 혜택이 서로 달라요. \\n그래서 **절세계좌니까 무조건 유리하다**고 판단하면 안 돼요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '혜택에는 조건이 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '절세계좌에는'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 납입 조건\\n- 유지 기간\\n- 인출 조건\\n- 투자 가능 상품')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '등의 조건이 붙을 수 있어요. \\n세금 혜택뿐 아니라 \\n**내가 그 조건을 지킬 수 있는지도 함께 봐야 해요.**')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 절세계좌는 일반계좌와 다른 세제 혜택을 받을 수 있는 계좌예요.\\n- 대표적인 혜택 방식에는 비과세, 세액공제, 과세이연 등이 있어요.\\n- 계좌마다 적용 조건과 받을 수 있는 혜택이 달라요.\\n- **세금 혜택만으로 가입 여부를 판단하기보다는 계좌 조건도 함께 확인해야 해요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'TAX-05';

-- TAX-06: 과세이연
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '어차피 낼 세금인데, 나중에 내면 뭐가 달라질까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '과세이연은 \\n세금을 없애주는 제도가 아니에요. \\n**지금 낼 세금을 나중으로 미루는 것**이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '비과세와 달라요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '헷갈리기 쉬운 두 개념을 비교하면'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- **비과세** → 일정 조건에서 세금 자체를 부과하지 않음\\n- **과세이연** → 세금을 내는 시점을 뒤로 미룸')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '즉 과세이연은 \\n세금이 사라지는 게 아니에요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '왜 의미가 있을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '세금을 지금 내지 않으면 \\n원래 세금으로 빠져나갈 돈이 계좌 안에 더 오래 남아 있을 수 있어요. \\n그 돈도 함께 운용된다면 시간이 지나면서 결과에 영향을 줄 수 있어요. \\n앞에서 배운 **복리**와 연결되는 부분이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '일반계좌와 비교하면',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '일반계좌에서는 소득이 발생할 때마다 \\n세금이 빠져나가는 구조가 있을 수 있어요. \\n과세이연이 적용되는 계좌에서는 \\n일정 시점까지 세금 납부를 뒤로 미룰 수 있어요. \\n그 차이로 인해 \\n**운용 중 남아 있는 자금 규모가 달라질 수 있어요.**')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 과세이연은 **세금 납부 시점을 뒤로 미루는 것**이에요.\\n- 과세이연은 비과세와는 다른 개념이에요.\\n- 세금으로 빠져나갈 돈이 일정 기간 더 운용될 수 있어요.\\n- **세금을 내는 시점과 적용 조건은 꼭 확인해야 해요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'TAX-06';

-- TAX-07: ISA
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '다들 ISA 얘기하는데 정확히 뭐가 다른 계좌일까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ISA는 여러 금융상품을 한 계좌에서 운용하면서 \\n일정 조건을 충족하면 세제 혜택을 받을 수 있도록 만든 계좌예요. \\n일반계좌와 가장 큰 차이는 \\n**계좌 자체에 별도의 세금 규칙이 있다는 점**이에요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', 'ISA의 세제 구조',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ISA의 혜택은 두 단계로 작동해요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '**① 일정 금액까지는 비과세가 적용돼요.** 계좌 안에서 발생한 손익을 합산한 **순이익**을 기준으로 해요. \\n**② 비과세 한도를 넘는 금액에는 별도의 세율로 분리과세가 적용돼요.** 다른 종합소득과 합산해 과세하지 않는 구조예요.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '소득 요건 등에 따라 일반형과 서민형으로 구분되고, 비과세 한도에도 차이가 있어요. \\n구체적인 한도와 세율은 제도에 따라 달라질 수 있어 가입 전 확인이 필요해요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '손익통산',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ISA의 특징 중 하나가 **손익통산**이에요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 계좌 안에서 어떤 투자에서는 이익이 나고 다른 투자에서는 손실이 났다면, 발생한 과세 대상 이익과 손실을 함께 계산하는 방식이에요.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '즉 이익만 따로 떼서 보는 것과는 달라요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '혜택만 보면 안 돼요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '**ISA의 세제 혜택을 받으려면 원칙적으로 3년의 의무가입기간을 충족해야 해요.**'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 의무가입기간 전에 중도해지하면 세제 혜택을 받지 못하거나 이미 받은 혜택이 추징될 수 있어요. *예외 사유가 있을 수 있어요\\n- 납입 한도와 가입 조건도 있고, **1인 1계좌**만 만들 수 있어요.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '따라서 가입할 때는 의무가입기간과 자금 사용 계획도 함께 확인할 필요가 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '계좌 하나 안에서 보면',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'ISA의 핵심은 특정 금융상품 하나가 아니라 **계좌의 세제 구조**예요. \\n같은 금융상품이라도 일반계좌에서 보유할 때와 ISA 안에서 보유할 때 결과가 달라질 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 6,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- ISA는 여러 금융상품을 담을 수 있는 절세계좌예요.\\n- 일정 조건을 충족하면 세제 혜택이 적용될 수 있어요.\\n- ISA에는 손익통산 구조가 있어요.\\n- **가입·납입·유지 조건을 함께 확인해야 해요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'TAX-07';

-- TAX-08: 연금저축
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '연금 받을 나이도 아닌데 왜 연금저축을 만들까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '이름 때문에 은퇴가 가까운 사람의 이야기처럼 느껴지지만, 연금저축은 지금 받을 수 있는 세액공제 혜택과 장기적인 노후 준비가 함께 연결된 계좌예요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '세액공제',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '연금저축에 납입한 금액 중 **일정 범위까지 세액공제**를 받을 수 있어요.'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('title', '세액공제', 'text', '세액공제는 내야 할 세금에서 직접 빼주는 방식이에요. 그래서 **연말정산에서 돌려받는 형태**로 나타나요.'),
        JSON_OBJECT('title', '공제율', 'text', '공제율은 **소득에 따라 달라져요.** 총급여가 낮을수록 공제율이 높습니다.')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '공제 한도와 공제율은 제도에 따라 달라질 수 있어 확인이 필요해요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '운용 중에는 과세이연',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '계좌 안에서 운용 중 발생한 수익에는 바로 세금을 부과하지 않고 인출 시점까지 과세를 미루는 구조예요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '연금으로 받을 때',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '연금저축은 \\n장기간 유지한 뒤 연금 형태로 수령하는 것을 전제로 설계된 계좌예요. \\n언제, 어떤 방식으로 받느냐에 따라 세금 처리도 달라질 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '중간에 꺼내면?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '노후를 위한 계좌이기 때문에 \\n세액공제를 받은 금액이나 운용수익을 연금 외 방식으로 인출하면 세금 부담이 커질 수 있어요. \\n즉 세액공제 혜택만 보고 \\n가까운 시기에 사용할 돈까지 넣는 것은 주의해야 해요.')
    )
  ),
  JSON_OBJECT(
    'order', 6,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- 연금저축은 노후 자금 마련과 세제 혜택이 연결된 계좌예요.\\n- 일정 범위에서 세액공제를 받을 수 있어요.\\n- 운용 중에는 과세이연 구조가 적용될 수 있어요.\\n- **연금 수령이나 중도인출 조건에 따라 세금이 달라질 수 있어요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'TAX-08';

-- TAX-09: IRP
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '연금저축이 있는데 IRP는 왜 또 필요할까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'IRP는 **개인형 퇴직연금 계좌**예요. \\n연금저축과 비슷하게 \\n노후 자금과 세제 혜택이 연결되지만, \\n운용 방법과 인출 조건에는 차이가 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 2,
    'title', '세액공제 구조',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '연금저축과 IRP는 세액공제 한도를 함께 적용해요. \\n현재 연금저축은 연 600만 원까지, IRP 등을 포함한 연금계좌는 합산해 연 900만 원까지 세액공제 대상이 될 수 있어요. \\n따라서 연금저축의 세액공제 한도를 채운 뒤 IRP에 추가 납입하면 합산 한도 안에서 공제 대상 금액을 늘릴 수 있어요.')
    )
  ),
  JSON_OBJECT(
    'order', 3,
    'title', '투자할 수 있는 자산에 제한이 있어요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', 'IRP는 퇴직연금 계좌라서 투자할 수 있는 자산과 비중에 제한이 있어요. \\n현재 위험자산은 전체 적립금의 **70%까지만** 투자할 수 있어요. \\n따라서 위험자산으로 분류되는 주식형 ETF만으로 계좌 전체를 채울 수는 없어요. \\n나머지는 예금처럼 위험자산 한도에 포함되지 않는 상품으로 운용해야 해요.')
    )
  ),
  JSON_OBJECT(
    'order', 4,
    'title', '중도인출도 더 제한적이에요',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '연금저축은 연금 수령 전에도 인출할 수 있지만 \\n세액공제를 받은 금액 등을 연금 외 방식으로 인출하면 세금이 발생할 수 있어요. \\n반면 IRP는 법에서 정한 사유가 아니면 중도인출이 제한돼요.')
    )
  ),
  JSON_OBJECT(
    'order', 5,
    'title', '연금저축과 비교하면',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'IMAGE', 'imageUrl', '')
    )
  ),
  JSON_OBJECT(
    'order', 6,
    'title', '어느 쪽이 더 좋을까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '중요한 건 \\n각 계좌는 목적과 조건이 다르기 때문에'),
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- **세제 혜택**\\n- **투자 제한**\\n- **인출 조건**')
      )),
      JSON_OBJECT('type', 'TEXT', 'text', '을 함께 비교해야 해요.')
    )
  ),
  JSON_OBJECT(
    'order', 7,
    'title', '핵심 정리',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'BOX', 'items', JSON_ARRAY(
        JSON_OBJECT('text', '- IRP는 개인형 퇴직연금 계좌예요.\\n- 세액공제와 과세이연 구조가 연결될 수 있어요.\\n- 위험자산 투자와 중도인출에 제한이 있을 수 있어요.\\n- **연금저축과 비슷하지만 조건과 운용 방식이 완전히 같지는 않아요.**')
      ))
    )
  )
),
updated_at = NOW()
WHERE content_code = 'TAX-09';

-- TAX-P: 세금용어·절세기초 (프리미엄)
UPDATE content SET body_data = JSON_ARRAY(
  JSON_OBJECT(
    'order', 1,
    'title', '세금 용어는 왜 이렇게 헷갈릴까요?',
    'content', JSON_ARRAY(
      JSON_OBJECT('type', 'TEXT', 'text', '공제와 감면, 비과세와 과세이연처럼 비슷해 보이는 말들이 많아 헷갈리시죠? 지금까지 나온 세금 용어를 한자리에 모아 정리해봐요.')
    )
  )
),
updated_at = NOW()
WHERE content_code = 'TAX-P';