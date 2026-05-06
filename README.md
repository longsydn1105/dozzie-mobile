# 🏨 Dozzie Hotel - Hotel Management & Booking Application

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-green.svg?style=flat)]()
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat)]()

**Dozzie Hotel** là ứng dụng di động hiện đại giúp tối ưu hóa trải nghiệm đặt phòng và quản lý dịch vụ khách sạn. Ứng dụng được xây dựng với hiệu suất cao, giao diện thân thiện và tích hợp các công nghệ mới nhất trong hệ sinh thái Android.

---

## 🚀 Tính năng chính (Key Features)

*   **🔐 Xác thực người dùng (Auth):** Đăng ký, đăng nhập bảo mật với JWT và Interceptor.
*   **🏠 Trang chủ (Home):** Hiển thị tổng quan các dịch vụ, phòng trống.
*   **📅 Đặt phòng (Booking):** Quy trình đặt phòng nhanh chóng, trực quan với quản lý trạng thái thời gian thực.
*   **💬 Chat trực tuyến:** Kết nối trực tiếp giữa khách hàng và bộ phận hỗ trợ thông qua Socket.io.
*   **📑 Quản lý hóa đơn (Invoice):** Theo dõi lịch sử thanh toán và chi tiết hóa đơn minh bạch.
*   **🎛️ Điều khiển thông minh (Control):** Tương tác hoặc điều khiển các thiết bị trong phòng.

---

## 🛠 Công nghệ sử dụng (Tech Stack)

Dự án áp dụng các thư viện và công nghệ tiên tiến nhất:

*   **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/) - Ngôn ngữ hiện đại, an toàn và súc tích.
*   **Dependency Injection:** [Koin](https://insert-koin.io/) - Framework DI nhẹ nhàng, dễ sử dụng cho Kotlin.
*   **Networking:** [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/) - Xử lý API gọi mạng và Interceptor cho Token.
*   **Database:** [Room Persistence](https://developer.android.com/training/data-storage/room) - Lưu trữ dữ liệu cục bộ mạnh mẽ.
*   **Local Storage:** [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) & [EncryptedSharedPreferences](https://developer.android.com/topic/security/data) - Lưu trữ cấu hình và token bảo mật.
*   **UI Framework:** ViewBinding,XML giúp quản lý luồng ứng dụng mượt mà.
*   **Asynchronous:** Kotlin Coroutines & Flow để xử lý các tác vụ bất đồng bộ.
*   **Image Loading:** [Coil](https://coil-kt.github.io/coil/) - Thư viện tải ảnh tối ưu dựa trên Coroutines.
*   **Real-time:** [Socket.io](https://socket.io/) cho tính năng chat thời gian thực.

---

## 🏗 Kiến trúc dự án (Architecture)

Dự án tuân thủ kiến trúc **MVVM (Model-ViewModel-View)** kết hợp với **Clean Architecture** principles để đảm bảo khả năng mở rộng và dễ dàng bảo trì:

*   **View:** Các Fragment/Activity đảm nhận hiển thị UI và nhận tương tác người dùng.
*   **ViewModel:** Xử lý logic nghiệp vụ và giữ trạng thái dữ liệu cho UI thông qua LiveData/Flow.
*   **Repository:** Đóng vai trò là nguồn dữ liệu duy nhất (Single Source of Truth), điều phối giữa Local Data (Room) và Remote Data (Retrofit).

---

## 📂 Cấu trúc thư mục (Project Structure)

```text
com.example.dozziehotel/
├── application/      # Cấu hình Application, Koin Modules
├── data/
│   ├── local/        # Room Database, DataStore, PreferenceManager
│   ├── remote/       # API Interfaces, AuthInterceptor, DTOs
│   ├── model/        # Các lớp dữ liệu (Entities, Models)
│   └── repository/   # Triển khai các Repository
├── di/               # Định nghĩa các Module Dependency Injection
├── ui/               # Các Feature-based packages
│   ├── auth/         # Login, Register
│   ├── home/         # Dashboard & Overview
│   ├── booking/      # Room Booking Flow
│   ├── chat/         # Real-time Messaging
│   ├── invoice/      # Billing & Payments
│   └── base/         # Các Base classes (BaseFragment, BaseViewModel)
└── utils/            # Các lớp tiện ích, Extension functions, Constants
```

---

## ⚙️ Cài đặt & Chạy thử

1.  Clone project:
    ```bash
    git clone https://github.com/your-repo/dozzie-mobile.git
    ```
2.  Mở project bằng **Android Studio (Ladybug hoặc mới hơn)**.
3.  Đảm bảo đã cài đặt JDK 17+.
4.  Đồng bộ Gradle và nhấn **Run** (Shift + F10).

---

## 🛡 Bảo mật

Dự án sử dụng `AuthInterceptor` để tự động đính kèm Bearer Token vào các yêu cầu API yêu cầu xác thực, đồng thời sử dụng `security-crypto` để mã hóa thông tin nhạy cảm lưu dưới máy.

---
📫 **Liên hệ:** [Phùng Sỹ Long] - [longsydn1105@gmail.com]
