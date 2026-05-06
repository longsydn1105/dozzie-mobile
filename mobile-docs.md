**PROJECT SPECIFICATION: DOZZIE MOBILE (KOTLIN)**

## **1\. TỔNG QUAN DỰ ÁN (PROJECT OVERVIEW)**

- **Tên dự án:** Dozzie Capsule Hotel - Mobile App.
- **Mục tiêu:** Cho phép khách hàng đặt phòng capsule, quản lý đơn đặt và tương tác với hệ thống IOT qua thiết bị Android

## **2\. TECH STACK (CÔNG NGHỆ)**

- **Language:** Kotlin.
- **UI Framework:** XML
- **Architecture:** MVVM (Model-View-ViewModel).
- **Networking:** Retrofit + OkHttp.
- **Local DB:** Room Database (để cache dữ liệu).
- **DI:** Koin.
- **Image Loading:** Coil.

## **3\. DESIGN SYSTEM (BẢNG MÃ MÀU & UI)**

**Ghi chú cho AI:** Hãy tuân thủ nghiêm ngặt bảng màu này khi viết Theme.

| **Thành phần**  | **Mã Màu (Hex)** | **Ý nghĩa**                   |
| --------------- | ---------------- | ----------------------------- |
| **Dozzie Blue** | #219EBC          | Màu chủ đạo (Primary)         |
| ---             | ---              | ---                           |
| **Dozzie Navy** | #18233B          | Màu nền đậm, Header           |
| ---             | ---              | ---                           |
| **Success**     | #10B981          | Trạng thái Active, Thành công |
| ---             | ---              | ---                           |
| **Danger**      | #EF4444          | Trạng thái Banned, Xóa, Lỗi   |
| ---             | ---              | ---                           |
| **Background**  | #F3F4F6          | Màu nền app                   |
| ---             | ---              | ---                           |

Nhớ lập 1 file riêng để quản lý mã màu

## **4\. DATABASE & MODELS (CẤU TRÚC DỮ LIỆU)**

**Mapping từ MongoDB sang Kotlin Data Classes.**

**Dưới đây là cơ sở dữ liệu gốc dựa trên Javascript**

### 4.1. User Model

const userSchema = new Schema(  
{  
email: { type: String, required: **true**, unique: **true**, trim: **true** },  
password: { type: String, required: **true** },  
fullName: { type: String, required: **true**, trim: **true** },  
phone: { type: String },  
role: { type: String, enum: \["user", "admin"\], default: "user" },  
status: { type: String, enum: \["active", "banned"\], default: "active" },  
},  
{  
timestamps: **true**,  
},  
);

### 4.2. Room Model

const roomSchema = new Schema(  
{  
// Mình tự đặt ID ("M-01", "F-01") nên định nghĩa \_id là String  
\_id: {  
type: String,  
required: true,  
uppercase: true,  
trim: true,  
},  
label: { type: String, required: true }, // Tên/Nhãn (vd: "Kén số 01")  
gender: { type: String, required: true, enum: \["Nam", "Nữ"\] }, // Khu Nam/Nữ  
floor: { type: Number, required: true }, // 2 cho nữ và 3 cho nam  
status: {  
type: String,  
enum: \["available", "occupied", "maintenance", "cleaning"\],  
default: "available",  
},  
iotConfig: {  
deviceId: { type: String, required: true },  
topicDoor: { type: String, required: true },  
topicPower: { type: String, required: true },  
isOnline: { type: Boolean, default: false },  
lastPing: { type: Date },  
},  
},  
{  
// Tắt \_id tự động của Mongoose vì ta đã dùng \_id custom ở trên  
\_id: false,  
timestamps: true, // Vẫn nên có  
},  
);

### 4.3 Booking Model

const bookingSchema = new Schema(  
{  
userId: { type: Schema.Types.ObjectId, ref: "User", required: true },  
roomId: { type: String, ref: "Room", required: true },  
packageId: { type: Schema.Types.ObjectId, ref: "ServicePackage", required: true },  
<br/>status: {  
type: String,  
enum: \["pending", "active", "completed", "cancelled", "admin_cancelled"\],  
default: "pending",  
},  
<br/>startTime: { type: Date, required: true },  
endTime: { type: Date, required: true },  
actualCheckIn: { type: Date }, // Updated when the digital key is used for the first time  
<br/>totalPrice: { type: Number, required: true },  
digitalKey: { type: String, required: true }, // Hash string for door unlocking  
},  
{  
timestamps: true,  
},  
);

###

###

### 4.4 Service Package Model

const servicePackageSchema = new mongoose.Schema(  
{  
name: { type: String, required: true },  
hours: { type: Number, required: true },  
price: { type: Number, required: true },  
isActive: { type: Boolean, default: true },  
},  
{  
timestamps: true,  
},  
);

### 4.5 Invoice Model

const invoiceSchema = new Schema({  
bookingId: { type: Schema.Types.ObjectId, ref: 'Booking', required: true },  
userId: { type: Schema.Types.ObjectId, ref: 'User', required: true },  
<br/>invoiceCode: { type: String, required: true, unique: true }, // e.g., INV-20260316-001  
<br/>roomCharge: { type: Number, required: true },  
extraFee: { type: Number, default: 0 }, // Additional charges (water, damages, etc.)  
totalAmount: { type: Number, required: true },  
<br/>paymentStatus: { type: String, enum: \['pending', 'paid', 'refunded'\], default: 'pending' },  
paidAt: { type: Date }  
}, {  
timestamps: true  
});

### 4.6 Review Model

const reviewSchema = new Schema(  
{  
userId: {  
type: Schema.Types.ObjectId,  
ref: "User",  
required: true,  
},  
bookingId: { type: Schema.Types.ObjectId, ref: "Booking", required: true },  
rating: {  
type: Number,  
required: true,  
min: 1,  
max: 5,  
},  
comment: {  
type: String,  
required: true,  
trim: true,  
},  
isShow: {  
type: Boolean,  
default: true,  
},  
},  
{  
timestamps: true, // Tự động có createdAt, updatedAt  
},  
);

### 4.7 Blog Model

const blogSchema = new Schema({  
title: { type: String, required: true, trim: true },  
slug: {  
type: String,  
required: true,  
unique: true,  
lowercase: true,  
trim: true  
},  
content: { type: String, required: true },  
<br/>// Liên kết tới người viết (Admin/User)  
authorId: { type: Schema.Types.ObjectId, ref: 'User', required: true },  
<br/>publishedAt: { type: Date, default: Date.now },  
tags: \[{ type: String }\], // Mảng các tag  
img_url: { type: String, required: false } // Ảnh bìa  
}, {  
timestamps: true  
});

### 4.8 SosAlert Model

const sosAlertSchema = new Schema(  
{  
userId: { type: Schema.Types.ObjectId, ref: "User", required: true },  
roomId: { type: String, ref: "Room", required: true },  
<br/>status: { type: String, enum: \["pending", "resolved"\], default: "pending" },  
message: { type: String, required: true }, // Reason for SOS  
resolvedAt: { type: Date }, // Timestamp when admin clicks "Resolve"  
},  
{  
timestamps: true,  
},  
);

### 4.9 Message Model
const messageSchema = new mongoose.Schema(
{
bookingId: { type: mongoose.Schema.Types.ObjectId, ref: "Booking", required: true },
roomId: { type: String, required: true },
senderId: { type: mongoose.Schema.Types.ObjectId, ref: "User" }, 
senderRole: { type: String, enum: ["customer", "admin"], required: true },
text: { type: String, required: true },
isRead: { type: Boolean, default: false },
},
{ timestamps: true },
);


## 5\. API ENDPOINT

### 5.1 User

**Base URL mặc định trên web:** <http://localhost:3000/api/users>

**Định dạng Header chung (Bắt buộc cho mọi API dưới đây):**

Authorization: Bearer &lt;chuỗi_token_của_user&gt;  
Content-Type: application/json

**Ghi chú cho AI/Mobile Dev:** Đây là API duy nhất trong module User mà App Mobile cần gọi.

#### 5.1.1. User Tự Cập Nhật Profile

- Mô tả: Khách hàng đổi tên, đổi số điện thoại hoặc đổi mật khẩu. An toàn tuyệt đối vì lấy ID từ Token, không sợ khách này truyền nhầm ID sửa thông tin khách kia.
- Endpoint: PUT /profile
- Quyền hạn (Auth): isAuth (Chỉ cần đăng nhập)

📥 Request Body (JSON): _(Tất cả các trường đều là Tùy chọn - Optional. Khách sửa trường nào thì gửi trường đó)_

{  
"fullName": "Long Phùng Đại Ca",  
"phone": "0987654321",  
"password": "newpassword123" // (Sẽ được tự động băm - hash bằng bcrypt ở BE)  
}

📤 Response - Thành công (200 OK):

{  
"success": true,  
"message": "Cập nhật profile thành công!",  
"data": {  
"\_id": "65abc123def...",  
"email": "<long@gmail.com>",  
"fullName": "Long Phùng Đại Ca",  
"phone": "0987654321",  
"role": "user",  
"status": "active",  
"createdAt": "2026-01-20T10:00:00Z",  
"updatedAt": "2026-03-31T15:00:00Z"  
// BẮT BUỘC KHÔNG TRẢ VỀ TRƯỜNG "password"  
}  
}

❌ Response - Thất bại (Lỗi hệ thống - 500):

{  
"success": false,  
"message": "Lỗi hệ thống khi cập nhật profile."  
}

### 5.2 Auth

Base URL mặc định: <http://localhost:3000/api/auth>

Headers: \* Content-Type: application/json

(Khu vực này Public, KHÔNG CẦN gửi Token Authorization)

#### 5.2.1. ĐĂNG KÝ TÀI KHOẢN (REGISTER)

- **Mô tả:** Khách hàng tạo tài khoản mới. Trả về thông tin user nhưng **không trả về Token** (khách phải tự login lại, hoặc App tự động chuyển sang trang Login sau khi đăng ký thành công).
- **Endpoint:** POST /register
- **Quyền hạn:** Public

**📥 Request Body (JSON):** _(Tất cả đều bắt buộc)_

JSON  
{  
"fullName": "Long Phùng",  
"email": "<long@dozzie.com>",  
"password": "mysecretpassword"  
}

**📤 Response - Thành công (201 Created):**

JSON  
{  
"success": true,  
"message": "Đăng ký tài khoản thành công.",  
"data": {  
"id": "65abc...",  
"fullName": "Long Phùng",  
"email": "<long@dozzie.com>",  
"role": "user"  
}  
}

**❌ Các trạng thái Lỗi (Cần bắt trên App Mobile để báo cho khách):**

- **400 Bad Request** (Thiếu data): { "success": false, "message": "Vui lòng nhập đầy đủ họ tên, email và mật khẩu." }
- **409 Conflict** (Trùng Email): { "success": false, "message": "Email này đã được đăng ký trên hệ thống." }
- **500 Internal Server Error** (Lỗi mạng/DB): { "success": false, "message": "Hệ thống đang gặp sự cố. Vui lòng thử lại sau.", "error": "..." }

**🧠 Business Logic (Luật chơi cho App):**

- Mobile Dev/AI cần validate form trước khi gọi API: Mật khẩu tối thiểu 6 ký tự, đúng định dạng email (tránh gọi API tốn băng thông).
- Khi nhận mã 201, App tự động Toast thông báo "Đăng ký thành công" và điều hướng (Navigate) về lại màn hình Login.

#### 5.2.2. ĐĂNG NHẬP (LOGIN)

- **Mô tả:** Xác thực tài khoản và cấp "Vé" (Token) để khách dùng các chức năng đặt phòng. App không cần phân biệt role, cứ đăng nhập thành công là nhét vào màn hình Home.
- **Endpoint:** POST /login
- **Quyền hạn:** Public

**📥 Request Body (JSON):** _(Bắt buộc)_

JSON  
{  
"email": "<long@dozzie.com>",  
"password": "mysecretpassword"  
}

**📤 Response - Thành công (200 OK):**

JSON  
{  
"success": true,  
"token": "eyJhbGciOiJIUzI1NiIsInR...", // Token này sống được 3 ngày  
"user": {  
"id": "65abc...",  
"fullName": "Long Phùng",  
"email": "<long@dozzie.com>",  
"role": "user"  
}  
}

**❌ Các trạng thái Lỗi (Cực kỳ quan trọng, App phải bắt đúng mã lỗi để báo):**

- **400 Bad Request** (Thiếu trường): { "message": "email hoặc password không được trống" }
- **404 Not Found** (Sai Email): { "message": "Email không tồn tại" }
- **403 Forbidden** (Bị khóa mõm - **Chốt chặn quan trọng**): { "success": false, "message": "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin!" }
- **400 Bad Request** (Sai Password): { "success": false, "message": "Mật khẩu sai rồi hoặc tài khoản rồi!" }

**🧠 Business Logic (Luật chơi cho App):**

- **Lưu Token:** Ngay khi nhận mã 200, App Kotlin phải lưu ngay chuỗi token vào **DataStore** hoặc **EncryptedSharedPreferences**.
- **Luồng Bị Khóa (403):** Nếu API nhả về 403, App hiển thị một Dialog đỏ chót báo lỗi, tuyệt đối không cho vượt qua trang Login.
- **Role Ignore:** Dù JSON trả về có role: "admin" hay role: "user", App Kotlin cứ coi như khách hàng bình thường, cho vào thẳng màn hình Home xem danh sách phòng. Không cần vẽ thêm UI quản trị.

### 5.3 Booking

**Base URL mặc định:** <http://localhost:3000/api/bookings>

**Định dạng Header chung (Bắt buộc cho mọi API ở đây):  
**

Authorization: Bearer &lt;chuỗi_token_của_user&gt;  
Content-Type: application/json

#### 5.3.1. TẠO ĐƠN ĐẶT PHÒNG (CREATE BOOKING)

- **Mô tả:** Khách hàng chốt đơn. App gửi thời gian bắt đầu, Backend sẽ tự cộng giờ theo Gói dịch vụ để ra thời gian kết thúc và tính tổng tiền.
- **Endpoint:** POST /
- **Quyền hạn:** isAuth (Khách đã đăng nhập)

**📥 Request Body (JSON):**

JSON  
{  
"roomId": "M-01",  
"packageId": "65abc123def...", // ID của gói dịch vụ khách chọn (VD: Gói 3h)  
"startTime": "2026-03-31T14:30:00.000Z" // BẮT BUỘC: Chuẩn ISO-8601 Date String  
}

**📤 Response - Thành công (201 Created):**

JSON  
{  
"success": true,  
"message": "Đặt phòng thành công! Mã mở cửa sẽ khả dụng khi đến giờ nhận phòng.",  
"data": {  
"\_id": "65def456...",  
"userId": "65abc...",  
"roomId": "M-01",  
"packageId": "65abc123def...",  
"startTime": "2026-03-31T14:30:00.000Z",  
"endTime": "2026-03-31T17:30:00.000Z",  
"totalPrice": 150000,  
"digitalKey": "482910", // Mã số để hiện lên màn hình App cho khách mở cửa  
"status": "pending",  
"createdAt": "2026-03-31T10:00:00.000Z"  
}  
}

**❌ Các trạng thái Lỗi cần bắt trên App:**

- **409 Conflict** (Trùng lịch - Rất hay xảy ra): { "success": false, "message": "Phòng này đã có người đặt trong khung giờ bạn chọn." }
- **404 Not Found:** { "message": "Gói dịch vụ không tồn tại." }

**🧠 Business Logic cho Mobile App:**

- **Tính giá nháp (Preview Price):** Trước khi gọi API này, App phải tự lấy price của Package hiển thị cho khách xem trước. API chỉ là bước chốt đơn cuối cùng.
- Khi thành công (201), App chuyển thẳng khách sang màn hình **"Chi tiết đơn"** để họ nhìn thấy cái digitalKey.

#### 5.3.2. LẤY LỊCH SỬ ĐẶT PHÒNG CỦA TÔI (MY BOOKINGS)

- **Mô tả:** Đổ danh sách các lần đặt phòng ra tab "Lịch sử". Backend đã tự động sắp xếp đơn mới nhất lên đầu.
- **Endpoint:** GET /my-bookings
- **Quyền hạn:** isAuth

**📥 Request:** Không cần Body.

**📤 Response - Thành công (200 OK):**

JSON  
{  
"success": true,  
"count": 2,  
"data": \[  
{  
"\_id": "65def456...",  
"status": "pending",  
"startTime": "2026-03-31T14:30:00.000Z",  
"endTime": "2026-03-31T17:30:00.000Z",  
"totalPrice": 150000,  
"roomId": {  
"\_id": "M-01",  
"label": "Phòng Đơn Tầng 1",  
"floor": 1  
},  
"packageId": {  
"\_id": "65abc123...",  
"name": "Gói 3 Giờ",  
"hours": 3,  
"price": 150000  
}  
}  
\]  
}

_Lưu ý: Dữ liệu roomId và packageId đã được Populate thành Object chứa thông tin chi tiết thay vì chỉ là chuỗi ID._

#### 5.3.3. XEM CHI TIẾT 1 ĐƠN CỤ THỂ

- **Mô tả:** Bấm vào 1 đơn trong Lịch sử để xem chi tiết (Mã Digital Key, quét QR, thời gian check-in...).
- **Endpoint:** GET /:id (Truyền ID booking vào URL. VD: /api/bookings/65def456...)
- **Quyền hạn:** isAuth _(nhớ kẹp isAuth vào route GET /:id bên Backend nhé)_

**📤 Response - Thành công (200 OK):** Trả về Object tương tự 1 item trong danh sách My Bookings ở trên, nhưng có kèm cả digitalKey.

#### 5.4.4. KHÁCH TỰ HỦY PHÒNG (CANCEL BOOKING)

- **Mô tả:** Cho phép khách đổi ý và hủy đơn.
- **Endpoint:** PATCH /:id/cancel (Truyền ID booking vào URL)
- **Quyền hạn:** isAuth

**📥 Request:** Không cần Body.

**📤 Response - Thành công (200 OK):**

JSON  
{  
"success": true,  
"message": "Đã hủy đơn thành công. Hẹn gặp lại lần sau!",  
"data": {  
"\_id": "...",  
"status": "cancelled" // Trạng thái đã được đổi  
}  
}

**❌ Các trạng thái Lỗi (Cần báo Alert):**

- **400 Bad Request:** { "message": "Không thể hủy phòng đang dùng hoặc đã dùng xong" } (Chỉ hủy được khi đang pending).
- **403 Forbidden:** { "message": "Không thể hủy đơn của người khác" } (Đề phòng hacker truyền bậy ID).

#### 5.4.5. KIỂM TRA PHÒNG TRỐNG (CHECK AVAILABILITY)

- **Mô tả:** Lấy danh sách các đơn đặt phòng sắp diễn ra. App Kotlin sẽ dựa vào thời gian của khách chọn để so sánh với danh sách này, từ đó "làm xám" (disable) các phòng đã có người xí chỗ.
- **Endpoint:** GET /
- **Quyền hạn:** isAuth (Bắt buộc phải truyền Token).

**📥 Request (Query Params):** App Kotlin chỉ gọi API lấy những đơn chưa kết thúc để giảm tải dữ liệu.

- status=pending (Hoặc status=active)
- _Ví dụ URL:_ /api/bookings?status=pending

**📤 Response - Thành công (200 OK):**

JSON  
{  
"success": true,  
"data": \[  
{  
"\_id": "65def...",  
"roomId": "M-01",  
"startTime": "2026-03-31T14:30:00.000Z",  
"endTime": "2026-03-31T17:30:00.000Z",  
"status": "pending"  
},  
{  
"\_id": "65def999...",  
"roomId": "M-02",  
"startTime": "2026-03-31T18:00:00.000Z",  
"endTime": "2026-03-31T20:00:00.000Z",  
"status": "pending"  
}  
\]  
}

**🧠 Business Logic cho App Kotlin (Cực kỳ quan trọng - AI bắt buộc phải đọc kỹ):**

Luồng thao tác trên App Kotlin phải chạy theo thứ tự sau:

- **Lắng nghe sự kiện:** Khi khách hàng chọn Thời gian bắt đầu và Gói dịch vụ trên giao diện -> App tự tính ra Thời gian kết thúc (Target End Time).
- **Quét dữ liệu (Lọc mảng):** App duyệt qua danh sách API trả về ở trên.
- **Thuật toán Check Trùng (Overlap Algorithm):** Phòng roomId bị coi là **CẤN LỊCH** nếu thỏa mãn điều kiện Toán học này: (Giờ Khách Bắt Đầu &lt; Giờ Đặt Kết Thúc) VÀ (Giờ Khách Kết Thúc &gt; Giờ Đặt Bắt Đầu)
- **Cập nhật UI (Jetpack Compose):** \* Áp dụng trạng thái isAvailable = false cho những phòng bị cấn.
  - Những phòng này trên giao diện Card sẽ bị: Chuyển sang màu Xám (#D1D5DB), tắt hiệu ứng bấm (modifier.clickable(enabled = false)), và đè thêm chữ "Đã kín lịch" màu Đỏ lên trên.
#### **5.4.6. LẤY TRẠNG THÁI HIỆN TẠI (GET MY STATUS)**

- **Mô tả:** Lấy trạng thái đặt phòng theo thời gian thực của người dùng. API này trả về 1 cục data duy nhất để App Mobile đưa ra quyết định: Có cho phép đặt phòng mới hay không, và có hiển thị nút "Điều khiển phòng thông minh" (Smart Key) hay không.
- **Endpoint:** GET /bookings/my-status
- **Quyền hạn:** isAuth (Khách đã đăng nhập - Lấy userId từ Token)

**📥 Request:** _Vì là phương thức GET và dùng Token định danh nên không yêu cầu truyền Body._

**📤 Response - Thành công (200 OK):**

JSON

{

"success": true,

"message": "Lấy trạng thái thành công!",

"data": {

"canBookNew": false,

"pendingCount": 1,

"activeBooking": {

"\_id": "65def456...",

"roomId": "M-01",

"packageId": "65abc123def...",

"startTime": "2026-04-29T20:00:00.000Z",

"endTime": "2026-05-01T12:00:00Z",

"digitalKey": "DK-9A8B7C",

"status": "active"

}

}

}

_(Lưu ý: Nếu người dùng không có phòng nào đang active, trường activeBooking sẽ trả về null. Nếu không có đơn nào đang vướng, canBookNew sẽ là true và pendingCount là 0)._

**❌ Các trạng thái cần bắt lỗi (Error Responses):**

- **401 Unauthorized:**
  - _Nguyên nhân:_ Khách chưa đăng nhập, không gửi kèm Token hoặc Token đã hết hạn.
  - _Response:_ { "success": false, "message": "Vui lòng đăng nhập để tiếp tục!" }
- **500 Internal Server Error:**
  - _Nguyên nhân:_ Lỗi kết nối Database trong quá trình truy vấn đơn hàng.
  - _Response:_ { "success": false, "message": "Lỗi hệ thống máy chủ!" }

**📱 Business Logic & Hướng dẫn xử lý cho App Mobile:**

API này được thiết kế theo mô hình BFF (Backend-For-Frontend), do đó App Kotlin chỉ cần gọi 1 lần khi mở App (hoặc pull-to-refresh) và chia dữ liệu cho các màn hình xử lý như sau:

**Tại Màn hình Trang chủ (Home Fragment):**

- - Đọc đối tượng data.activeBooking.
  - **Nếu null:** làm mờ nút bấm liên quan đến điều khiển phòng thông minh(bấm vào sẽ sẽ thông báo bạn không thể điều khiển phòng).
  - **Nếu có dữ liệu:**
    - Hiển thị Card/Nút nhấn "Bảng điều khiển phòng {roomId}".
    - Khi khách hàng nhấn vào nút này, App sẽ lấy trực tiếp roomId và digitalKey từ cục JSON này truyền sang Màn hình Điều khiển (Control Fragment) qua Navigation Arguments, không cần gọi thêm API nào khác để lấy chìa khóa.
  - Đọc cờ data.canBookNew.
  - **Nếu true:** Cho phép chọn giờ, chọn phòng và hiện sáng nút "Đặt ngay".
  - **Nếu false:** Vô hiệu hóa (Disable) nút "Đặt ngay" và hiển thị cảnh báo đỏ: _"Bạn đang có {pendingCount} đơn hàng chờ xử lý hoặc đang sử dụng phòng. Vui lòng hoàn tất trước khi đặt mới!"_.

# 5.4.7 API Specification: Customer Early Checkout

## Endpoint

```
PATCH /api/bookings/:id/checkout
```

## Authentication

- **Required**: Bearer Token (JWT)
- **Header**: `Authorization: Bearer <token>`

## URL Parameters

| Param | Type              | Description             |
| ----- | ----------------- | ----------------------- |
| `id`  | string (ObjectId) | Booking ID cần checkout |

## Request Body

```json
{}
```

**Note**: Request body có thể gửi empty object hoặc omit toàn bộ body.

## Success Response (200 OK)

```json
{
  "success": true,
  "message": "Checkout sớm thành công.",
  "data": {
    "_id": "64f1c2a8...",
    "userId": "64e9d1f2...",
    "roomId": "101",
    "packageId": "64ea2b1c...",
    "status": "completed",
    "startTime": "2026-05-06T10:00:00.000Z",
    "endTime": "2026-05-06T14:00:00.000Z",
    "actualCheckIn": null,
    "totalPrice": 500000,
    "digitalKey": "654321",
    "isReminded10Min": false,
    "createdAt": "2026-05-06T09:45:00.000Z",
    "updatedAt": "2026-05-06T11:30:00.000Z"
  }
}
```

## Error Responses

### 404 - Booking Not Found

```json
{
  "success": false,
  "message": "Không tìm thấy đơn này."
}
```

### 403 - Unauthorized (Not Owner)

```json
{
  "success": false,
  "message": "Không thể checkout đơn của người khác."
}
```

### 400 - Invalid Status

```json
{
  "success": false,
  "message": "Chỉ có thể checkout khi booking đang ở trạng thái active."
}
```

### 500 - Server Error

```json
{
  "success": false,
  "message": "Lỗi hệ thống khi checkout booking."
}
```

## Request Examples

### cURL

```bash
curl -X PATCH http://localhost:3000/api/bookings/64f1c2a8.../checkout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json"
```

### JavaScript (Fetch API)

```javascript
const bookingId = "64f1c2a8...";
const token = "eyJhbGciOiJIUzI1NiIs...";

fetch(`http://localhost:3000/api/bookings/${bookingId}/checkout`, {
  method: "PATCH",
  headers: {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  },
})
  .then((res) => res.json())
  .then((data) => console.log(data));
```

### REST Client (VSCode Extension)

```http
@baseUrl = http://localhost:3000/api
@token = eyJhbGciOiJIUzI1NiIs...

### Checkout Booking
PATCH {{baseUrl}}/bookings/64f1c2a8.../checkout
Authorization: Bearer {{token}}
```

## Logic Flow

1. Lấy `bookingId` từ URL params
2. Lấy `userId` từ JWT token (auth middleware)
3. Tìm booking theo `bookingId` trong database
4. Kiểm tra booking tồn tại → nếu không trả 404
5. Kiểm tra booking thuộc về user hiện tại → nếu không trả 403
6. Kiểm tra booking `status === "active"` → nếu không trả 400
7. Cập nhật `status` → `"completed"`
8. Lưu booking vào database
9. Trả về booking đã cập nhật kèm status 200

## Business Rules

- ✅ Chỉ user sở hữu booking mới được checkout
- ✅ Chỉ booking ở trạng thái `active` mới được checkout sớm
- ✅ Sau khi checkout, trạng thái thành `completed`
- ✅ Timestamp `updatedAt` tự động cập nhật khi save
- ✅ Khách không thể checkout booking ở trạng thái:
  - `pending` (chưa thanh toán)
  - `completed` (đã checkout rồi)
  - `cancelled` (đã hủy)
  - `admin_cancelled` (admin đã hủy)

## Status Code Reference

| Code | Meaning                                                     |
| ---- | ----------------------------------------------------------- |
| 200  | Checkout thành công                                         |
| 400  | Booking không ở trạng thái active hoặc request không hợp lệ |
| 403  | User không phải chủ sở hữu booking                          |
| 404  | Booking không tồn tại                                       |
| 500  | Lỗi server                                                  |

## Response Fields Description

| Field                  | Type            | Description                                    |
| ---------------------- | --------------- | ---------------------------------------------- |
| `success`              | boolean         | Kết quả thành công hay thất bại                |
| `message`              | string          | Thông báo chi tiết (tiếng Việt)                |
| `data`                 | object          | Booking object sau khi cập nhật (chỉ có ở 200) |
| `data._id`             | string          | ID của booking                                 |
| `data.userId`          | string          | ID của user (chủ booking)                      |
| `data.roomId`          | string          | ID của phòng                                   |
| `data.packageId`       | string          | ID của gói dịch vụ                             |
| `data.status`          | string          | Trạng thái booking (luôn là "completed")       |
| `data.startTime`       | ISO8601         | Thời gian bắt đầu                              |
| `data.endTime`         | ISO8601         | Thời gian kết thúc dự kiến                     |
| `data.actualCheckIn`   | ISO8601 \| null | Thời gian check-in thực tế                     |
| `data.totalPrice`      | number          | Giá tiền tổng (VND)                            |
| `data.digitalKey`      | string          | Mã khóa số 6 chữ số                            |
| `data.isReminded10Min` | boolean         | Đã gửi nhắc nhở 10 phút chưa                   |
| `data.createdAt`       | ISO8601         | Thời gian tạo booking                          |
| `data.updatedAt`       | ISO8601         | Thời gian cập nhật gần nhất                    |

## Notes

- API yêu cầu authentication, khách hàng phải gửi token valid
- Booking phải ở trạng thái `active` để checkout sớm được
- Sau khi checkout, booking sẽ chuyển sang `completed` ngay lập tức
- Không hoàn tiền hay chiết khấu nếu checkout sớm (tuỳ chính sách hệ thống)
- Nếu khách checkout sớm, cron job vẫn sẽ chạy nhưng không ảnh hưởng vì booking đã `completed`


### 5.4 Invoice

**Base URL mặc định:** <http://localhost:3000/api/invoices>

**Định dạng Header chung (Bắt buộc cho mọi API ở đây):  
**

Authorization: Bearer &lt;chuỗi_token_của_user&gt;  
Content-Type: application/json

#### 5.4.1. LẤY DANH SÁCH HÓA ĐƠN CỦA TÔI

**Mô tả:** Lấy toàn bộ danh sách lịch sử hóa đơn của khách hàng đang đăng nhập. Dữ liệu trả về tự động lồng ghép (populate) thêm chi tiết chuyến đi (phòng nào, gói dịch vụ nào, mấy giờ) và được sắp xếp từ mới nhất đến cũ nhất.

**Endpoint:** GET /my-invoices

**Quyền hạn:** isAuth (Khách hàng đăng nhập)

📥 **Request:** Không cần Body.

📤 **Response - Thành công (200 OK):**

{  
"success": true,  
"data": \[  
{  
"\_id": "65ab...",  
"invoiceCode": "INV-17130012345-456",  
"roomCharge": 150000,  
"extraFee": 0,  
"totalAmount": 150000,  
"paymentStatus": "pending",  
"bookingId": {  
"\_id": "65ac...",  
"roomId": "M-01",  
"packageId": {  
"\_id": "65ad...",  
"name": "Combo Nghỉ Trưa",  
"hours": 3  
}  
},  
"createdAt": "2026-04-17T14:00:00.000Z"  
}  
\]}

❌ **Các trạng thái Lỗi (Cần báo Alert):**

- **500 Internal Server Error:** { "message": "Lỗi hệ thống..." } (Lỗi từ database không lấy được dữ liệu).

####

#### 5.4.2. KHÁCH THANH TOÁN HÓA ĐƠN

**Mô tả:** Khách hàng xác nhận thanh toán cho một hóa đơn cụ thể. Hệ thống sẽ tự động đổi trạng thái hóa đơn thành paid, ghi nhận giờ thanh toán (paidAt), và đồng thời "kích hoạt" luôn phòng (Booking status -> active) để khách sử dụng.

**Endpoint:** PATCH /:id/pay (Truyền ID của hóa đơn vào URL)

**Quyền hạn:** isAuth (Khách hàng đăng nhập)

📥 **Request:** Không cần Body.

📤 **Response - Thành công (200 OK):**

JSON  
{  
"success": true,  
"message": "Thanh toán thành công! Chúc bạn có trải nghiệm tuyệt vời.",  
"data": {  
"\_id": "65ab...",  
"invoiceCode": "INV-17130012345-456",  
"totalAmount": 150000,  
"paymentStatus": "paid",  
"paidAt": "2026-04-17T14:05:00.000Z"  
}  
}

❌ **Các trạng thái Lỗi (Cần báo Alert):**

- **404 Not Found:** { "message": "Không tìm thấy hóa đơn của bạn." } (Xảy ra khi truyền sai ID, hoặc hacker cố tình truyền ID hóa đơn của người khác để thanh toán giùm/phá hoại).
- **400 Bad Request:** { "message": "Hóa đơn này đã được xử lý từ trước!" } (Xảy ra khi khách spam nút thanh toán, hoặc hóa đơn đã bị hủy/đã thanh toán rồi nhưng app vẫn gọi API).

### 5.5 Room

**Base URL mặc định:** <http://localhost:3000/api/rooms>

**Định dạng Header chung (Bắt buộc cho mọi API ở đây):  
**

Authorization: Bearer &lt;chuỗi_token_của_user&gt;  
Content-Type: application/json

**5.5.1 Điều khiển phòng**

- **Mô tả:** Gửi lệnh điều khiển phần cứng (Mở cửa, Đóng cửa, SOS) xuống phòng Kén thông qua giao thức MQTT. API này đóng vai trò như một "trạm gác", nó sẽ kiểm tra tính hợp lệ của digitalKey và thời gian thuê phòng hiện tại trước khi thực thi lệnh.
- **Endpoint:** POST /rooms/iot-command
- **Quyền hạn:** isAuth (Khách đã đăng nhập)

**📥 Request Body (JSON):**

JSON

{

"roomId": "M-01",

"digitalKey": "DK-9A8B7C",

"topic": "dozzie/capsule/M-01",

"payload": {

"command": "DOOR_OPEN"

}

}

_(Ghi chú cho Dev: payload có thể truyền thẳng chuỗi "DOOR_OPEN" hoặc object { "command": "DOOR_OPEN" }, Server đều có cơ chế tự động trích xuất lệnh để gửi xuống MQTT)._

**📤 Response - Thành công (200 OK):**

JSON

{

"success": true,

"message": "Lệnh đã được thực thi!"

}

**❌ Các trạng thái cần bắt lỗi (Error Responses):**

- **400 Bad Request:**
  - _Nguyên nhân:_ App truyền thiếu một trong các trường bắt buộc (roomId, digitalKey, topic, payload).
  - _Response:_ { "success": false, "message": "Thiếu thông tin điều khiển!" }
- **403 Forbidden (Lỗi Chìa khóa/Trạng thái):**
  - _Nguyên nhân:_ Sai digitalKey, sai roomId, hoặc Booking đang ở trạng thái pending (chưa thanh toán/chưa kích hoạt).
  - _Response:_ { "success": false, "message": "Chìa khóa không hợp lệ hoặc Booking chưa được kích hoạt (Pending)!" }
- **403 Forbidden (Lỗi Thời gian):**
  - _Nguyên nhân:_ Giờ gọi lệnh không nằm trong khoảng startTime và endTime của đơn đặt phòng.
  - _Response:_ { "success": false, "message": "Chìa khóa đã hết hạn hoặc chưa đến giờ sử dụng!" }
- **500 Internal Server Error:**
  - _Nguyên nhân:_ Lỗi phía máy chủ (Node.js/MongoDB).
  - _Response:_ { "success": false, "message": "Lỗi hệ thống!" }

**📱 Business Logic & Hướng dẫn xử lý cho App Mobile (Kotlin):**

- **Chuẩn bị Dữ liệu (Tiền xử lý):**
  - App KHÔNG tự sinh ra roomId và digitalKey.
  - Hai thông số này phải được lấy từ kết quả của API GET /bookings/my-status (ở màn hình Home) và truyền sang màn hình Điều khiển (Control Fragment).
  - Tham số topic được App nối chuỗi tự động theo công thức: "dozzie/capsule/" + roomId.
- **Xử lý Giao diện (UI/UX) khi bấm nút (Mở/Đóng/SOS):**
  - **Loading:** Khi bắt đầu gọi API, disable (khóa) tất cả các nút điều khiển và hiện vòng xoay Loading để tránh khách bấm spam liên tục nhiều lệnh.
  - **Success (200):** Tắt Loading, hiện Toast/Snackbar màu xanh: _"Đã gửi lệnh thành công!"_.
  - **Error (400, 403, 500):** Tắt Loading, đọc field message từ response lỗi của Server và hiện Toast màu đỏ cho khách hàng biết lý do (VD: "Chìa khóa đã hết hạn").
  - **Đặc biệt lưu ý:** Hàm sendCommandToRoom ở Server có dùng qos: 1 (đảm bảo lệnh đến phần cứng ít nhất 1 lần). Do đó App Mobile chỉ cần gọi API 1 lần duy nhất cho mỗi cú click, tuyệt đối không được viết logic tự động retry (thử lại) nếu thấy Server chậm phản hồi, để tránh tình trạng kẹt/spam motor cửa.

#### **5.2.1. LẤY DANH SÁCH TOÀN BỘ PHÒNG (GET ALL ROOMS)**

Mô tả: Lấy danh sách toàn bộ các kén/phòng trong hệ thống. Thường dùng để render bản đồ 2D cho khách chọn phòng.

Endpoint: GET /

Quyền hạn: Public (Không yêu cầu đăng nhập, không cần truyền Header Authorization)

📥 Request: Không cần Body.

📤 Response - Thành công (200 OK):

JSON  
{  
"message": "Lấy list phòng thành công",  
"count": 40,  
"data": \[  
{  
"\_id": "F-01",  
"label": "Kén Nữ 01",  
"gender": "Nữ",  
"floor": 2,  
"status": "available",  
"iotConfig": {  
"isOnline": true  
}  
},  
{  
"\_id": "M-01",  
"label": "Kén Nam 01",  
"gender": "Nam",  
"floor": 1,  
"status": "occupied"  
}  
\]  
}

❌ Các trạng thái Lỗi cần bắt trên App: 500 Internal Server Error: { "message": "Server lỗi khi lấy phòng." }

🧠 Business Logic cho Mobile App:

- **Lọc UI:** App cần dựa vào trường gender để chia Layout thành Khu Nam / Khu Nữ.
- **Chặn tương tác:** App phải đọc trường status. Nếu là available thì cho phép bấm vào. Nếu là occupied (đang có người), maintenance (bảo trì) hoặc cleaning (đang dọn dẹp) thì bắt buộc phải làm mờ cục UI đó đi (disable touch) và hiện icon báo bận.

### 5.6 Service Package

#### 1\. Lấy Danh Sách Gói Dịch Vụ Đang Hoạt Động

- **Mô tả:** Lấy danh sách toàn bộ các gói dịch vụ (Combo giờ) đang được mở bán (isActive: true). Dữ liệu trả về đã được Backend tự động sắp xếp theo số giờ từ ít đến nhiều (tăng dần).
- **Endpoint:** GET /packages
- **Quyền hạn:** Public (Không yêu cầu đăng nhập)
- **Request:** Không cần Body.
- **Response - Thành công (200 OK):**
- JSON

{  
"success": true,  
"data": \[  
{  
"\_id": "65abc123...",  
"name": "Combo Nghỉ Trưa",  
"hours": 3,  
"price": 150000,  
"isActive": true,  
"createdAt": "2026-01-01T10:00:00.000Z",  
"updatedAt": "2026-01-01T10:00:00.000Z"  
},  
{  
"\_id": "65abc456...",  
"name": "Combo Qua Đêm",  
"hours": 12,  
"price": 350000,  
"isActive": true,  
"createdAt": "2026-01-01T10:05:00.000Z",  
"updatedAt": "2026-01-01T10:05:00.000Z"  
}  
\]  
}

- **Lỗi (500):** { "success": false, "message": "Lỗi hệ thống..." }

🧠 **Business Logic cho Mobile App:**

- **Hiển thị UI:** App sẽ gọi API này ở màn hình Đặt phòng (Sau khi khách đã chọn Kén). Dùng mảng data này để render ra các Nút/Thẻ chọn gói giờ (VD: Nút "3 Giờ - 150.000đ", "12 Giờ - 350.000đ").
- **Lưu trữ biến:** Khi khách bấm chọn 1 gói, App phải lưu trữ lại cái \_id của gói đó (Đây chính là cái packageId bắt buộc phải truyền vào Body lúc gọi API Tạo Booking).
- **Tính giá (Preview):** App tự động lấy trường price của gói được chọn để hiển thị "Tổng thanh toán tạm tính" cho khách nhìn thấy trước khi chốt đơn.

#### **5.7 LẤY LỊCH SỬ TRÒ CHUYỆN (GET CHAT HISTORY)**

- **Mô tả:** Lấy danh sách toàn bộ tin nhắn cũ của một đơn đặt phòng. App Mobile cần gọi API này ĐẦU TIÊN ngay khi khách hàng mở màn hình Chat để hiển thị các tin nhắn trước đó, sau đó mới tiến hành kết nối WebSocket.
- **Endpoint:** GET /api/chat/history/:bookingId
- **Quyền hạn:** isAuth (Khách đã đăng nhập - Yêu cầu truyền Token vào Header)

**📥 Request Params (URL):**

- bookingId: ID của đơn đặt phòng hiện tại (Lấy từ API my-status).

**📤 Response - Thành công (200 OK):**

JSON

- - {
  - "success": true,
  - "data": \[
  - {
  - "\_id": "60d5ec...",
  - "bookingId": "65def456...",
  - "roomId": "M-01",
  - "senderRole": "admin",
  - "text": "Chào bạn, lễ tân Dozzie xin nghe!",
  - "isRead": true,
  - "createdAt": "2026-05-02T10:00:00.000Z"
  - },
  - {
  - "\_id": "60d5ed...",
  - "bookingId": "65def456...",
  - "roomId": "M-01",
  - "senderRole": "customer",
  - "text": "Cho mình xin thêm cái khăn tắm nha",
  - "isRead": false,
  - "createdAt": "2026-05-02T10:05:00.000Z"
  - }
  - \]
  - }

_(Lưu ý: Mảng data đã được Server sắp xếp sẵn theo thứ tự thời gian từ cũ tới mới, App chỉ việc render thẳng vào RecyclerView)._

**❌ Các trạng thái cần bắt lỗi:**

- **401 Unauthorized:** Khách chưa đăng nhập / Token hết hạn.
- **500 Internal Server Error:** Lỗi truy vấn Database.

#### **5.8 KẾT NỐI WEBSOCKET (LIVE CHAT SERVER)**

- **Mô tả:** Cổng giao tiếp thời gian thực (Real-time) sử dụng giao thức Socket.io để nhắn tin 2 chiều giữa Khách hàng và Lễ tân.
- **Thư viện yêu cầu cho Kotlin:** implementation('io.socket:socket.io-client:2.1.0') _(BẮT BUỘC dùng thư viện Socket.io, không dùng WebSocket thuần)._
- **Base URL Kết nối (Production):** \[<https://dozzie-server.onrender.com\>]
  - ⚠️ _Đặc biệt lưu ý cho Dev Mobile:_ KHÔNG thêm /api vào sau URL. Hệ thống Render đã tự động xử lý cổng kết nối, App Kotlin chỉ cần dùng cú pháp: mSocket = IO.socket("\[<https://dozzie-server.onrender.com\>](<https://dozzie-server.onrender.com>)")

**A. CÁC SỰ KIỆN APP KOTLIN CẦN GỬI LÊN (EMIT TO SERVER):**

**1\. Sự kiện báo danh vào phòng:** join_chat

- _Thời điểm gọi:_ Ngay sau khi hàm socket.connect() báo thành công.
- _Payload (JSON):_

JSON

- - {
  - "bookingId": "65def456...",
  - "role": "customer"
  - }

**2\. Sự kiện gửi tin nhắn mới:** send_message

- _Thời điểm gọi:_ Khi người dùng bấm nút "Gửi" trên bàn phím.
- _Payload (JSON):_

JSON

- - {
  - "bookingId": "65def456...",
  - "roomId": "M-01",
  - "senderRole": "customer",
  - "text": "Nội dung tin nhắn khách gõ..."
  - }

**B. SỰ KIỆN APP KOTLIN CẦN LẮNG NGHE (LISTEN FROM SERVER):**

**1\. Sự kiện có tin nhắn tới:** receive_message

- _Thời điểm lắng nghe:_ Khai báo lắng nghe ngay sau khi báo danh vào phòng thành công.
- _Data nhận được (JSON):_ Trả về đúng 1 Object (là tin nhắn vừa được lưu vào DB).

JSON

- - {
  - "\_id": "60d5ee...",
  - "bookingId": "65def456...",
  - "roomId": "M-01",
  - "senderRole": "admin",
  - "text": "Dạ, nhân viên đang mang lên cho bạn ạ!",
  - "isRead": false,
  - "createdAt": "2026-05-02T10:10:00.000Z"
  - }

**📱 Business Logic & Hướng dẫn xử lý cực kỳ quan trọng cho Dev Kotlin:**

- **Luồng khởi tạo chuẩn:**
  - Mở màn hình Chat -> Gọi API GET /api/chat/history -> Cập nhật danh sách lên UI.
  - Thiết lập socket.connect() -> Đợi connect xong -> Gọi socket.emit("join_chat", ...) -> Khai báo sự kiện socket.on("receive_message", ...).
- **Xử lý UI tin nhắn mới:** Khi nhận được sự kiện receive_message, hãy check trường senderRole.
  - Nếu senderRole == "customer": Đây là tin nhắn do chính mình gửi, hiển thị Bubble Chat màu xanh bên Phải.
  - Nếu senderRole == "admin": Tin nhắn của Lễ tân, hiển thị Bubble Chat màu xám bên Trái.
- **Cảnh báo chống lặp tin nhắn (Anti-Duplication):** TUYỆT ĐỐI KHÔNG đặt hàm mSocket.on("receive_message") vào các hàm Lifecycle chạy nhiều lần như onResume(), onStart() hoặc bên trong sự kiện click nút bấm. Phải đặt ở trong init của ViewModel hoặc đảm bảo đã gọi mSocket.off("receive_message") trước khi đăng ký mới để tránh bị lỗi hiển thị đúp tin nhắn.
- **Dọn dẹp bộ nhớ:** Khi người dùng thoát khỏi màn hình Chat (hàm onCleared() của ViewModel hoặc onDestroy() của Fragment), BẮT BUỘC gọi socket.disconnect() để giải phóng tài nguyên.
- Thời gian booking không hợp lệ(hết giờ rồi thì không nhắn được)

### **5.9 Hệ thống Cứu hộ Khẩn cấp (SOS Alert)**

**Base URL mặc định: <http://localhost:3000/api/sos> _(hoặc \[<https://dozzie-server.onrender.com/api/sos\>](<https://dozzie-server.onrender.com/api/sos>) trên Production)_**

**Định dạng Header chung (Bắt buộc cho mọi API ở đây):**

**Plaintext**

- - **Authorization: Bearer &lt;chuỗi_token_của_user_hoặc_admin&gt;**
  - **Content-Type: application/json**

#### **5.9.1. GỬI TÍN HIỆU CỨU HỘ (CREATE SOS ALERT)**

- **Mô tả: Khách hàng bấm nút SOS trên App Mobile khi gặp sự cố khẩn cấp trong phòng. API sẽ ghi nhận vào Database với trạng thái pending.**
- **Endpoint: POST /emergency**
- **Quyền hạn: isAuth (Khách đã đăng nhập đang ở trong phòng)**

**📥 Request Body (JSON):**

**JSON**

- - **{**
  - **"roomId": "M-01",**
  - **"message": "Cửa phòng bị kẹt không mở được!"**
  - **}**

**_(Lưu ý: Không cần truyền userId vì Server sẽ tự động bóc tách từ Token trong Header)._**

**📤 Response - Thành công (201 Created):**

**JSON**

- - **{**
  - **"success": true,**
  - **"message": "Yêu cầu cứu hộ đã được gửi!"**
  - **}**

**❌ Các trạng thái Lỗi cần bắt trên App:**

- **401 Unauthorized: Khách chưa đăng nhập hoặc Token hết hạn.**
- **500 Internal Server Error: Lỗi máy chủ không lưu được.**

**🧠 Business Logic cho Mobile App (Kotlin):**

- **Nút bấm: Nên thiết kế dạng "Nhấn giữ 3 giây" hoặc "Trượt để gọi SOS" để tránh việc khách bấm nhầm.**
- **Real-time: App Mobile chỉ cần gọi API này là đủ. Việc phát loa còi hú báo động cho Admin sẽ do Server đảm nhiệm thông qua Socket.io (sự kiện ADMIN_SOS_ALERT như đã bàn).**
### 6. ARCHITECTURE & FOLDER STRUCTURE (CẤU TRÚC THƯ MỤC)

_Ghi chú CỰC KỲ QUAN TRỌNG cho AI (Cursor): Tuân thủ nghiêm ngặt cấu trúc gói (package) MVVM dưới đây. Tuyệt đối KHÔNG tạo thư mục ở ngoài root. Mọi file Kotlin phải được tạo bên trong đường dẫn gốc là: `app/src/main/java/com/example/dozziehotel/`_

**Package gốc:** `com.example.dozziehotel`

```text
app/src/main/java/com/example/dozziehotel/
│
├── application/         # Quản lý vòng đời ứng dụng
│   └── DozzieApplication.kt  # Khởi tạo Koin, Timber, App State...
│
├── data/                # Tầng dữ liệu (Model & Repo)
│   ├── local/           # Room Database, DataStore (lưu Token)
│   ├── remote/          # Retrofit API Interfaces, Interceptors
│   ├── model/           # Kotlin Data Classes (DTOs & Entities)
│   └── repository/      # Implementations của Repository
│
├── di/                  # Dependency Injection (Koin Modules)
│   ├── NetworkModule.kt # Cung cấp Retrofit, OkHttpClient
│   ├── DatabaseModule.kt# Cung cấp RoomDB, Dao
│   ├── RepoModule.kt    # Cung cấp Repositories
│   └── ViewModelModule.kt# Cung cấp ViewModels
│
├── ui/                  # Tầng Giao diện (Activity, Fragment, ViewModel)
│   ├── base/            # BaseActivity, BaseFragment, BaseViewModel
│   ├── auth/            # Login, Register
│   ├── home/            # Sơ đồ phòng, Kén
│   ├── booking/         # Đặt phòng, Chọn gói giờ
│   └── invoice/         # Hóa đơn, Thanh toán
│
└── utils/               # Các hàm dùng chung
    ├── Constants.kt     # BASE_URL, SharedPrefs Keys...
    ├── Resource.kt      # Wrapper class quản lý State (Success, Error, Loading)
    └── Extensions.kt    # View.visible(), View.gone(), Toast...

## **7\. CORE COMPONENTS & LIFECYCLE (THÀNH PHẦN CỐT LÕI)**

### **7.1. App Lifecycle (Application Class)**

- AI cần tạo file DozzieApplication.kt kế thừa từ Application().
- Trong onCreate(), bắt buộc khởi tạo **Koin** (startKoin) và nạp các Modules (Network, DB, Repo, ViewModel).
- Khai báo class này vào AndroidManifest.xml (android:name=".application.DozzieApplication").

### **7.2. Network Interceptor (Quản lý Token tự động)**

- Yêu cầu AI viết một AuthInterceptor chèn vào OkHttpClient.
- Interceptor này sẽ tự động lấy Token từ DataStore/SharedPreferences và gắn vào Header Authorization: Bearer &lt;token&gt; cho mọi API call, trừ các route Public (như /login, /register).

### **7.3. State Management (Resource Wrapper)**

Để UI biết khi nào hiện xoay vòng Loading, khi nào báo Lỗi, AI cần tạo một class Resource&lt;T&gt; (Sealed Class) trong mục utils:

sealed class Resource&lt;T&gt;(val data: T? = null, val message: String? = null) {
class Success&lt;T&gt;(data: T) : Resource&lt;T&gt;(data)
class Error&lt;T&gt;(message: String, data: T? = null) : Resource&lt;T&gt;(data, message)
class Loading&lt;T&gt;(data: T? = null) : Resource&lt;T&gt;(data)
}

### **7.4. Điều hướng (Navigation)**

- Sử dụng **Android Navigation Component** (Jetpack) với 1 MainActivity duy nhất (Single-Activity Architecture).
- Các màn hình (Login, Home, Booking) đều là Fragment.
- Cần có 1 file nav_graph.xml để định nghĩa luồng di chuyển.

## 8. THƯ VIỆN & CÔNG NGHỆ BẮT BUỘC (DEPENDENCIES)
*Ghi chú cho AI: Khi setup build.gradle và viết code, BẮT BUỘC tuân thủ các công nghệ sau:*
- **UI Binding:** Sử dụng `ViewBinding` (Tuyệt đối không dùng `findViewById` hay `Kotlin Synthetics`).
- **Bất đồng bộ:** Sử dụng `Coroutines` và `Flow` / `StateFlow` (Tuyệt đối không dùng RxJava hay Callbacks).
- **Network:** Retrofit2 + OkHttp3 Logging Interceptor + Gson Converter.
- **Local Storage:** Room Database (Cache) và `EncryptedSharedPreferences` / `DataStore` (Lưu Token).
- **DI:** `Koin` cho Android.
- **Image:** `Coil`.
```
