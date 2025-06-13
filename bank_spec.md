# BANK API

## 1. 사용자 생성

- **URL**: `POST /api/bank/user`

    - **Request Body**
      ```json
      [
        {
          "username" : "구동혁",
          "phoneNumber" : "010-2331-2976",
          "userTypeCode" : 0
        }
      ]
      ```

    - **Response (200)**
      ```json
        {
          "isSuccess" : "true",
          "code" : "200",
          "message": "사용자 생성에 성공 하였습니다."
        }
      ```

---

## 2. 상품 조회

- **URL**: `GET /api/bank/good`
- **Response (200)**
  ```json
  {
    "isSuccess" : "true",
	   "code" : "200",
    "message": "상품 조회 성공: 상품 수 = 2.",
	   "result" : [
		    {
				"id" : 1,
				"name" : "기본 예금 상품",
				"accountTypeCode" : "예금", 
				"interestRate": 1.5,
				"interestCycle": 30
		   },
		   {
				"id" : 1,
				"name" : "기본 적금 상품",
				"accountTypeCode" : "적금", 
				"interestRate": 2.0,
				"interestCycle": 30
		    }
	    ] 
    }
  ```
---

## 3. 계좌 개설

- **URL**: `POST  /api/bank/account`
- **Request Body**
  ```json
  [
    {
      "username" : "구동혁",
      "phoneNumber" : "010-2331-2976",
      "accountTypeCode": "1",
      "userTypeCode" : 0
    }
  ]
  ```

- **Response (200)**

  ```json
   {
	   "isSuccess" : "true",
	   "code" : "200",
    "message": "계좌 생성이 완료되었습니다."
   }
  ```

---

## 4. 출금 이체

- **URL**: `POST  /api/bank/transfer/withdraw`
- **Request Body**
  ```json
  {
    "transactionId": "123e4567-e89b-12d3-a456-426614174000",
    "withdrawBankCode": 1,
    "withdrawAccount": "626-02-9397-352593",
    "transferAmount": 10000,
    "currencyCode": 0,
    "counterAccount": "111-11-1111-111111",
    "counterBankCode": 1
  }
  ```
- **Response (200)**

  ```json
  {
	  "isSuccess" : "true",
	  "code" : "200",
   "message": "출금 이체에 성공하였습니다."
  }
  ```

---

## 5. 입금 이체

- **URL**: `POST  /api/bank/transfer/deposit`
- **Request Body**
  ```json
  {
    "transactionId": "123e4567-e89b-12d3-a456-426614174000",
    "depositBankCode": 1,
    "depositAccount": "626-02-9397-352593",
    "transferAmount": 50000,
    "currencyCode": 0,
    "counterAccount": "111-11-1111-111111",
    "counterBankCode": 1
  }
  ```
- **Response (200)**

  ```json
  {
	  "isSuccess" : "true",
	  "code" : "200",
   "message": "입금 이체에 성공하였습니다."
  }
  ```

---

## 6. 계좌 목록 조회

- **URL**: `POST  /api/bank/account/search`
- **Request Body**
  ```json
  {
    "username": "홍길동",
    "phoneNumber": "010-1234-5678"
  }
  ```

- **Response (200)**

  ```json
    {
    "isSuccess": true,
    "code": 1000,
    "message": "요청에 성공하였습니다.",
    "result": [
    {
				"accountNumber" : "110495304567",
				"balance" : 1000000,
				"bankCode": 0,
				"accountStatusCode": 1,
				"accountTypeCode" : 1, 
				"withdrawLimit": 3000000,
				"createdAt": "2024-04-23",
				"updatedAt": "2024-04-23"
		},
		{
				"accountNumber" : "110164390706",
				"balance" : 1000000,
				"bankCode": 0,
				"accountStatus": 1,
				"accountTypeCode" : 1, 
				"withdrawLimit": 3000000,
				"createdAt": "2024-04-23",
				"updatedAt": "2024-04-23"
		  }
   ]
  }
  ```

---

## 7. 계좌 거래 내역 조회

- **URL**: `POST /api/bank/account/history`
- **Request Body**
  ```json
    {
     "account": "626-02-8261-248396"
    }
  ```

- **Response (200)**

  ```json
   {
     "isSuccess": true,
     "code": 1000,
     "message": "요청에 성공하였습니다.",
     "result": [
        {
            "transferType": "DEPOSIT",
            "account": "626-02-9397-352593",
            "counterpartAccount": "111-11-1111-111111",
            "transferAmount": 50000,
            "currencyCode": "WON",
            "createdAt": "2025-04-28T16:46:18.776339"
        },
        {
            "transferType": "WITHDRAW",
            "account": "626-02-9397-352593",
            "counterpartAccount": "111-11-1111-111111",
            "transferAmount": 10000,
            "currencyCode": "WON",
            "createdAt": "2025-04-28T16:48:54.192684"
        }
    ]
   }
  ```

---

## 8. 계좌 잔액 확인

- **URL**: `POST /api/bank/account/balance`
- **Request Body**
  ```json
    {
     "account": "626-02-8261-248396"
    }
  ```

- **Response (200)**

  ```json
   {
     "isSuccess": true,
    "code": "ACCOUNT2004",
    "message": "계좌 잔액 조회에 성공하였습니다.",
    "result": {
        "balance": 10000000
    }
   }
  ```
  
---

## 9. 예금주명 조회

- **URL**: `POST /api/bank/account/owner`
- **Request Body**
  ```json
    {
     "account": "626-02-8261-248396"
    }
  ```

- **Response (200)**

  ```json
   {
	  "isSuccess" : "true",
	  "code" : "200",
    "message": "해당 계좌번호의 소유자(예금주)를 확인하였습니다.",
	   "result" : 
     {
        "username" : "홍길동"
	    }
  }
  ```

---

## 10. 계좌 유효성 검사

- **URL**: `POST /api/bank/account/valid`
- **Request Body**
  ```json
    {
     "username" : "홍길동",
     "account": "626-02-8261-248396"
    }
  ```

- **Response (200)**

  ```json
   {
    "isSuccess" : "true",
	   "code" : "200",
    "message": "예금주와 계좌 정보가 일치합니다."
   }
  ```

---

## 11. 휴면 계좌 여부 검사

- **URL**: `POST /api/bank/account/dormant`
- **Request Body**
  ```json
    {
     "accountNumber": "626-02-8261-248396"
    }
  ```

- **Response (200)**

  ```json
   {
    "isSuccess" : "true",
	   "code" : "200",
     "message": "휴면 계좌 여부 조회에 성공했습니다.",
	   "result" : 
		   {
			"isDormant" : true
		   }
   }
  ```

---

## 11. 잔액 및 송금 한도 검사

- **URL**: `POST /api/bank/account/transferable`
- **Request Body**
  ```json
    {
     "username": "구동혁",
     "account" : "626-02-9108-347753",
     "transferAmount" : 4000001
    }
  ```

- **Response (200)**

  ```json
   {
    "isSuccess": true,
    "code": "ACCCOUNT2008",
    "message": "해당 계좌는 송금 처리 가능합니다.",
    "result": {
        "balance": 100000,
        "withdrawLimit": 300000,
        "transferable": true
    }
}
  ```

---