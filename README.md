# FileMe雲端硬碟
(Readme建構中...)

## 技術
### 後端及資料庫
> Java / Spring Boot / Spring Security / MyBatis / Thymeleaf / Redis / MySQL
- 使用Spring Boot作為後端應用程式框架，持久層則使用MyBatis介接MySQL資料庫。
- 以Spring Security搭配JWT進行身份驗證及授權管理。
- 透過Redis實作Stateless backend及Session管理，並且用於暫存email token。
- 使用Thymeleaf建立系統通知email模板。

> Logback / Maven / Git / OpenAPI(Swagger) / Postman
- 使用Logback搭配Spring AOP機制記錄系統日誌(log)。
- 透過Maven進行套件管理，使用Git協助系統版本控制。
- 以OpenAPI產生RESTful API文件，使用Postman測試API。

### 雲端
> AWS EC2 / RDS / ElastiCache
> S3 / CloudFront / Route53
> VPC / NAT 
### 前端
> JavaScript / AJAX / jQuery / CSS(SASS) / HTML5
- 使用SASS進行樣式管理
- 使用jQuery UI建立右鍵選單、dialog元件

## 功能概述
### 會員中心
> 各項功能皆以系統驗證信進行身份確認
- 註冊
- 資料變更(密碼、信箱)
- 忘記密碼

### 目錄與檔案管理
#### Create
- 新增目錄、新增檔案
#### Read
- 查看目錄
- 預覽檔案
- 下載檔案
- 分享檔案連結
- 模糊搜尋(依相關度排列：關鍵字數量、關鍵字位置)
#### Update
- 更改目錄和檔案名稱
- 權限管理
- 移動目錄和檔案位置
- 移至垃圾桶
- 從垃圾桶還原
#### Delete
- 立即刪除
