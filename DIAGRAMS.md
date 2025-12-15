# Dokumentasi Diagram Sistem HerbaMed Jabar

Dokumen ini berisi analisis dan diagram sistem untuk aplikasi HerbaMed Jabar, sebuah aplikasi Android untuk identifikasi tanaman herbal menggunakan AI.

## Daftar Isi
1. [ERD (Entity Relationship Diagram)](#erd-entity-relationship-diagram)
2. [Diagram Konteks](#diagram-konteks)
3. [DFD Level 0](#dfd-level-0)
4. [DFD Level 1](#dfd-level-1)
5. [Flowmap Sistem](#flowmap-sistem)

---

## ERD (Entity Relationship Diagram)

ERD menunjukkan struktur data dan relasi antar entitas dalam sistem HerbaMed Jabar.

```mermaid
erDiagram
    USER ||--o{ SCAN_HISTORY : "melakukan"
    USER ||--o{ POST : "membuat"
    USER ||--o{ LIKE : "memberikan"
    POST ||--o{ LIKE : "memiliki"
    SCAN_HISTORY ||--o| POST : "dapat dibagikan sebagai"

    USER {
        string userId PK "Firebase Auth UID"
        string email
        string displayName
        string profilePictureUrl
        timestamp createdAt
    }

    SCAN_HISTORY {
        int id PK "Auto-generated"
        string resultText "Hasil analisis AI"
        string imagePath "Path gambar lokal"
        long timestamp "Waktu scan"
    }

    POST {
        string id PK "Firestore Document ID"
        string userId FK "ID pengguna"
        string username "Nama pengguna"
        string userProfilePictureUrl "URL foto profil"
        string imageUrl "URL gambar di Cloudinary"
        string plantName "Nama tanaman"
        string description "Deskripsi pengguna"
        string content "Deskripsi dari AI"
        string benefit "Manfaat tanaman"
        string warning "Peringatan"
        long timestamp "Waktu posting"
        array likes "Array user IDs yang like"
    }

    LIKE {
        string postId FK "ID post"
        string userId FK "ID pengguna"
    }
```

### Penjelasan Entitas:

1. **USER** (Firebase Authentication)
   - Dikelola oleh Firebase Authentication
   - Menyimpan informasi autentikasi dan profil pengguna
   - Mendukung login email/password dan Google Sign-In

2. **SCAN_HISTORY** (Room Database - Lokal)
   - Menyimpan riwayat pemindaian tanaman
   - Disimpan secara lokal di perangkat pengguna
   - Gambar disimpan di internal storage

3. **POST** (Firebase Firestore)
   - Menyimpan postingan forum pengguna
   - Gambar di-upload ke Cloudinary
   - Dapat dibuat dari hasil scan atau manual

4. **LIKE** (Embedded dalam POST)
   - Relasi many-to-many antara User dan Post
   - Disimpan sebagai array dalam dokumen Post

---

## Diagram Konteks

Diagram Konteks menunjukkan interaksi sistem dengan entitas eksternal.

```mermaid
flowchart TB
    User((Pengguna))
    Admin((Admin/Moderator))
    
    subgraph HerbaMedSystem["Sistem HerbaMed Jabar"]
        App[Aplikasi Android]
    end
    
    FirebaseAuth[Firebase Authentication]
    FirebaseFirestore[Firebase Firestore]
    Cloudinary[Cloudinary API]
    GeminiAI[Google Gemini AI]
    
    User -->|Login/Register| HerbaMedSystem
    User -->|Scan Tanaman| HerbaMedSystem
    User -->|Lihat Forum| HerbaMedSystem
    User -->|Posting ke Forum| HerbaMedSystem
    User -->|Like Post| HerbaMedSystem
    
    HerbaMedSystem -->|Hasil Identifikasi| User
    HerbaMedSystem -->|Riwayat Scan| User
    HerbaMedSystem -->|Feed Forum| User
    
    HerbaMedSystem <-->|Autentikasi| FirebaseAuth
    HerbaMedSystem <-->|Data Forum & Likes| FirebaseFirestore
    HerbaMedSystem -->|Upload Gambar| Cloudinary
    Cloudinary -->|URL Gambar| HerbaMedSystem
    HerbaMedSystem -->|Gambar + Prompt| GeminiAI
    GeminiAI -->|Analisis Tanaman| HerbaMedSystem
    
    Admin -.->|Monitor| FirebaseFirestore
    
    style HerbaMedSystem fill:#4CAF50
    style User fill:#2196F3
    style Admin fill:#FF9800
```

### Entitas Eksternal:

1. **Pengguna**: User utama yang menggunakan aplikasi
2. **Admin/Moderator**: Mengelola konten melalui Firebase Console
3. **Firebase Authentication**: Layanan autentikasi
4. **Firebase Firestore**: Database cloud untuk forum
5. **Cloudinary**: Layanan penyimpanan dan hosting gambar
6. **Google Gemini AI**: AI untuk analisis tanaman

---

## DFD Level 0

DFD Level 0 menunjukkan proses utama dalam sistem.

```mermaid
flowchart TB
    User((Pengguna))
    
    subgraph System["Sistem HerbaMed Jabar"]
        P1[1.0<br/>Manajemen<br/>Autentikasi]
        P2[2.0<br/>Pemindaian<br/>Tanaman]
        P3[3.0<br/>Manajemen<br/>Forum]
        P4[4.0<br/>Manajemen<br/>Riwayat]
        P5[5.0<br/>Manajemen<br/>Profil]
    end
    
    D1[(D1: Local DB<br/>scan_history)]
    D2[(D2: Firestore<br/>posts)]
    D3[(D3: Firebase Auth<br/>users)]
    
    ExtAI[Google Gemini AI]
    ExtCloud[Cloudinary]
    
    User -->|kredensial login/register| P1
    P1 -->|status autentikasi| User
    P1 <-->|data user| D3
    
    User -->|gambar tanaman| P2
    P2 -->|request analisis| ExtAI
    ExtAI -->|hasil analisis| P2
    P2 -->|hasil identifikasi| User
    P2 -->|data scan| D1
    
    User -->|buat/lihat post| P3
    P3 <-->|data post| D2
    P3 -->|upload gambar| ExtCloud
    ExtCloud -->|URL gambar| P3
    P3 -->|feed forum| User
    
    User -->|request riwayat| P4
    P4 <-->|data riwayat| D1
    P4 -->|daftar riwayat| User
    
    User -->|request profil| P5
    P5 <-->|data user| D3
    P5 <-->|post user| D2
    P5 -->|info profil| User
    
    style System fill:#E8F5E9
    style P1 fill:#81C784
    style P2 fill:#81C784
    style P3 fill:#81C784
    style P4 fill:#81C784
    style P5 fill:#81C784
```

---

## DFD Level 1

### DFD Level 1.0 - Manajemen Autentikasi

```mermaid
flowchart TB
    User((Pengguna))
    
    subgraph P1["1.0 Manajemen Autentikasi"]
        P11[1.1<br/>Login]
        P12[1.2<br/>Register]
        P13[1.3<br/>Google Sign-In]
        P14[1.4<br/>Logout]
    end
    
    D3[(D3: Firebase Auth<br/>users)]
    
    User -->|email, password| P11
    P11 -->|verifikasi kredensial| D3
    D3 -->|data user| P11
    P11 -->|status login| User
    
    User -->|data registrasi| P12
    P12 -->|buat akun baru| D3
    D3 -->|konfirmasi| P12
    P12 -->|status register| User
    
    User -->|Google token| P13
    P13 -->|verifikasi token| D3
    D3 -->|data user| P13
    P13 -->|status login| User
    
    User -->|request logout| P14
    P14 -->|clear session| D3
    P14 -->|konfirmasi| User
```

### DFD Level 1.1 - Pemindaian Tanaman

```mermaid
flowchart TB
    User((Pengguna))
    
    subgraph P2["2.0 Pemindaian Tanaman"]
        P21[2.1<br/>Ambil Gambar]
        P22[2.2<br/>Analisis dengan AI]
        P23[2.3<br/>Parse Hasil]
        P24[2.4<br/>Simpan Riwayat]
        P25[2.5<br/>Tampilkan Hasil]
    end
    
    D1[(D1: Local DB<br/>scan_history)]
    ExtAI[Google Gemini AI]
    LocalStorage[Local Storage]
    
    User -->|akses kamera| P21
    P21 -->|bitmap gambar| P22
    P22 -->|gambar + prompt| ExtAI
    ExtAI -->|teks analisis| P22
    P22 -->|raw result| P23
    P23 -->|structured data| P25
    P23 -->|data + gambar| P24
    P24 -->|simpan gambar| LocalStorage
    LocalStorage -->|path gambar| P24
    P24 -->|record scan| D1
    P25 -->|hasil terstruktur| User
```

### DFD Level 1.2 - Manajemen Forum

```mermaid
flowchart TB
    User((Pengguna))
    
    subgraph P3["3.0 Manajemen Forum"]
        P31[3.1<br/>Lihat Feed]
        P32[3.2<br/>Buat Post]
        P33[3.3<br/>Upload Gambar]
        P34[3.4<br/>Like/Unlike]
        P35[3.5<br/>Hapus Post]
    end
    
    D2[(D2: Firestore<br/>posts)]
    D3[(D3: Firebase Auth<br/>users)]
    ExtCloud[Cloudinary]
    
    User -->|request feed| P31
    P31 -->|query posts| D2
    D2 -->|list posts| P31
    P31 -->|feed| User
    
    User -->|data post + gambar| P32
    P32 -->|upload| P33
    P33 -->|gambar| ExtCloud
    ExtCloud -->|URL| P33
    P33 -->|URL| P32
    P32 -->|get user info| D3
    D3 -->|user data| P32
    P32 -->|post baru| D2
    
    User -->|toggle like| P34
    P34 -->|update likes array| D2
    D2 -->|konfirmasi| P34
    P34 -->|status| User
    
    User -->|delete request| P35
    P35 -->|remove post| D2
    D2 -->|konfirmasi| P35
    P35 -->|status| User
```

### DFD Level 1.3 - Manajemen Riwayat

```mermaid
flowchart TB
    User((Pengguna))
    
    subgraph P4["4.0 Manajemen Riwayat"]
        P41[4.1<br/>Ambil Riwayat]
        P42[4.2<br/>Lihat Detail]
        P43[4.3<br/>Hapus Riwayat]
    end
    
    D1[(D1: Local DB<br/>scan_history)]
    LocalStorage[Local Storage]
    
    User -->|request list| P41
    P41 -->|query all| D1
    D1 -->|list scan_history| P41
    P41 -->|daftar riwayat| User
    
    User -->|select item| P42
    P42 -->|get by id| D1
    D1 -->|detail record| P42
    P42 -->|load gambar| LocalStorage
    LocalStorage -->|gambar| P42
    P42 -->|detail lengkap| User
    
    User -->|delete request| P43
    P43 -->|remove record| D1
    D1 -->|konfirmasi| P43
    P43 -->|status| User
```

### DFD Level 1.4 - Manajemen Profil

```mermaid
flowchart TB
    User((Pengguna))
    
    subgraph P5["5.0 Manajemen Profil"]
        P51[5.1<br/>Lihat Profil]
        P52[5.2<br/>Lihat Post User]
        P53[5.3<br/>Lihat Statistik]
    end
    
    D2[(D2: Firestore<br/>posts)]
    D3[(D3: Firebase Auth<br/>users)]
    
    User -->|request profil| P51
    P51 -->|get user data| D3
    D3 -->|info user| P51
    P51 -->|profil| User
    
    User -->|request my posts| P52
    P52 -->|query by userId| D2
    D2 -->|user posts| P52
    P52 -->|daftar post| User
    
    User -->|request stats| P53
    P53 -->|count posts| D2
    D2 -->|jumlah| P53
    P53 -->|statistik| User
```

---

## Flowmap Sistem

Flowmap menunjukkan alur interaksi pengguna dengan sistem secara keseluruhan.

### Flowmap 1: Alur Autentikasi

```mermaid
flowchart TD
    Start([Mulai Aplikasi])
    CheckAuth{User<br/>Terautentikasi?}
    Splash[Splash Screen]
    AuthScreen[Layar Autentikasi]
    ChooseAuth{Pilih Metode}
    
    LoginScreen[Form Login]
    RegisterScreen[Form Register]
    GoogleLogin[Google Sign-In]
    
    ValidateInput{Input<br/>Valid?}
    ProcessAuth[Proses Autentikasi<br/>Firebase]
    AuthSuccess{Berhasil?}
    
    MainActivity[Main Activity<br/>Bottom Navigation]
    ErrorMsg[Tampilkan Error]
    
    Start --> Splash
    Splash --> CheckAuth
    CheckAuth -->|Tidak| AuthScreen
    CheckAuth -->|Ya| MainActivity
    
    AuthScreen --> ChooseAuth
    ChooseAuth -->|Login| LoginScreen
    ChooseAuth -->|Register| RegisterScreen
    ChooseAuth -->|Google| GoogleLogin
    
    LoginScreen --> ValidateInput
    RegisterScreen --> ValidateInput
    GoogleLogin --> ProcessAuth
    
    ValidateInput -->|Valid| ProcessAuth
    ValidateInput -->|Tidak Valid| ErrorMsg
    ErrorMsg --> ChooseAuth
    
    ProcessAuth --> AuthSuccess
    AuthSuccess -->|Ya| MainActivity
    AuthSuccess -->|Tidak| ErrorMsg
    
    style Start fill:#4CAF50
    style MainActivity fill:#4CAF50
    style ErrorMsg fill:#F44336
```

### Flowmap 2: Alur Pemindaian Tanaman

```mermaid
flowchart TD
    Start([User di Home])
    ClickScan[Klik Tab Scan]
    ScanScreen[Layar Scan]
    ChooseMethod{Pilih Metode}
    
    OpenCamera[Buka Kamera]
    TakePhoto[Ambil Foto]
    SelectGallery[Pilih dari Galeri]
    
    PreviewImage[Preview Gambar]
    ConfirmScan{Lanjut Scan?}
    
    ShowProgress[Tampilkan<br/>Processing Dialog]
    CallAI[Kirim ke Gemini AI]
    ReceiveResult[Terima Hasil Analisis]
    ParseResult[Parse Markdown Result]
    SaveLocal[Simpan ke Local DB<br/>+ Internal Storage]
    
    ResultScreen[Layar Hasil<br/>Detail Tanaman]
    ChooseAction{Pilih Aksi}
    
    ShareForum[Bagikan ke Forum]
    UploadCloud[Upload ke Cloudinary]
    SaveFirestore[Simpan ke Firestore]
    ForumSuccess[Post Berhasil]
    
    ViewHistory[Lihat Riwayat]
    BackHome[Kembali ke Home]
    ErrorAI[Error: Gagal Analisis]
    
    Start --> ClickScan
    ClickScan --> ScanScreen
    ScanScreen --> ChooseMethod
    
    ChooseMethod -->|Kamera| OpenCamera
    ChooseMethod -->|Galeri| SelectGallery
    
    OpenCamera --> TakePhoto
    TakePhoto --> PreviewImage
    SelectGallery --> PreviewImage
    
    PreviewImage --> ConfirmScan
    ConfirmScan -->|Ya| ShowProgress
    ConfirmScan -->|Tidak| ChooseMethod
    
    ShowProgress --> CallAI
    CallAI --> ReceiveResult
    ReceiveResult -->|Berhasil| ParseResult
    ReceiveResult -->|Gagal| ErrorAI
    ParseResult --> SaveLocal
    SaveLocal --> ResultScreen
    
    ResultScreen --> ChooseAction
    ChooseAction -->|Bagikan| ShareForum
    ChooseAction -->|Lihat Riwayat| ViewHistory
    ChooseAction -->|Selesai| BackHome
    
    ShareForum --> UploadCloud
    UploadCloud --> SaveFirestore
    SaveFirestore --> ForumSuccess
    ForumSuccess --> BackHome
    
    ErrorAI --> ScanScreen
    ViewHistory --> BackHome
    BackHome --> Start
    
    style Start fill:#4CAF50
    style ResultScreen fill:#2196F3
    style ForumSuccess fill:#4CAF50
    style ErrorAI fill:#F44336
```

### Flowmap 3: Alur Forum

```mermaid
flowchart TD
    Start([User di Home])
    ClickForum[Klik Tab Forum]
    ForumScreen[Layar Forum<br/>Feed Posts]
    LoadPosts[Load Posts dari Firestore<br/>Order by Timestamp DESC]
    
    DisplayFeed[Tampilkan Feed]
    UserAction{Pilih Aksi}
    
    ViewPost[Lihat Detail Post]
    LikePost[Like/Unlike Post]
    UpdateLike[Update Likes Array<br/>di Firestore]
    RefreshFeed[Refresh Feed]
    
    CreatePost[Buat Post Baru]
    FillForm[Isi Form Post]
    SelectImage[Pilih Gambar]
    ValidateForm{Form<br/>Lengkap?}
    
    UploadImage[Upload ke Cloudinary]
    GetUserInfo[Ambil Info User<br/>dari Firebase Auth]
    SavePost[Simpan Post ke Firestore]
    PostSuccess[Post Berhasil]
    
    ViewProfile[Lihat Profil]
    MyPosts[Post Saya]
    DeletePost{Hapus Post?}
    ConfirmDelete[Konfirmasi Hapus]
    RemoveFromFirestore[Hapus dari Firestore]
    
    BackHome[Kembali]
    ErrorPost[Error: Gagal Post]
    
    Start --> ClickForum
    ClickForum --> ForumScreen
    ForumScreen --> LoadPosts
    LoadPosts --> DisplayFeed
    
    DisplayFeed --> UserAction
    UserAction -->|Lihat Detail| ViewPost
    UserAction -->|Like| LikePost
    UserAction -->|Buat Post| CreatePost
    UserAction -->|Profil| ViewProfile
    UserAction -->|Kembali| BackHome
    
    ViewPost --> UserAction
    
    LikePost --> UpdateLike
    UpdateLike --> RefreshFeed
    RefreshFeed --> DisplayFeed
    
    CreatePost --> FillForm
    FillForm --> SelectImage
    SelectImage --> ValidateForm
    ValidateForm -->|Tidak| FillForm
    ValidateForm -->|Ya| UploadImage
    UploadImage --> GetUserInfo
    GetUserInfo --> SavePost
    SavePost -->|Berhasil| PostSuccess
    SavePost -->|Gagal| ErrorPost
    PostSuccess --> RefreshFeed
    ErrorPost --> CreatePost
    
    ViewProfile --> MyPosts
    MyPosts --> DeletePost
    DeletePost -->|Ya| ConfirmDelete
    DeletePost -->|Tidak| ViewProfile
    ConfirmDelete --> RemoveFromFirestore
    RemoveFromFirestore --> RefreshFeed
    
    BackHome --> Start
    
    style Start fill:#4CAF50
    style PostSuccess fill:#4CAF50
    style ErrorPost fill:#F44336
```

### Flowmap 4: Alur Riwayat

```mermaid
flowchart TD
    Start([User di Home])
    ClickHistory[Klik Tab History]
    HistoryScreen[Layar Riwayat]
    LoadHistory[Load dari Local DB<br/>Order by Timestamp DESC]
    
    CheckData{Ada<br/>Riwayat?}
    EmptyState[Tampilkan Empty State]
    DisplayList[Tampilkan List Riwayat]
    
    UserAction{Pilih Aksi}
    SelectItem[Pilih Item]
    DetailScreen[Layar Detail Riwayat]
    LoadImage[Load Gambar dari<br/>Internal Storage]
    DisplayDetail[Tampilkan Detail]
    
    DetailAction{Pilih Aksi}
    ShareForum[Bagikan ke Forum]
    DeleteItem[Hapus Riwayat]
    ConfirmDelete{Konfirmasi<br/>Hapus?}
    
    RemoveDB[Hapus dari Local DB]
    DeleteSuccess[Berhasil Dihapus]
    
    BackHistory[Kembali ke List]
    BackHome[Kembali ke Home]
    
    Start --> ClickHistory
    ClickHistory --> HistoryScreen
    HistoryScreen --> LoadHistory
    LoadHistory --> CheckData
    
    CheckData -->|Tidak| EmptyState
    CheckData -->|Ya| DisplayList
    
    EmptyState --> BackHome
    
    DisplayList --> UserAction
    UserAction -->|Pilih Item| SelectItem
    UserAction -->|Kembali| BackHome
    
    SelectItem --> DetailScreen
    DetailScreen --> LoadImage
    LoadImage --> DisplayDetail
    
    DisplayDetail --> DetailAction
    DetailAction -->|Bagikan| ShareForum
    DetailAction -->|Hapus| DeleteItem
    DetailAction -->|Kembali| BackHistory
    
    ShareForum --> BackHistory
    
    DeleteItem --> ConfirmDelete
    ConfirmDelete -->|Ya| RemoveDB
    ConfirmDelete -->|Tidak| DisplayDetail
    RemoveDB --> DeleteSuccess
    DeleteSuccess --> BackHistory
    
    BackHistory --> DisplayList
    BackHome --> Start
    
    style Start fill:#4CAF50
    style DeleteSuccess fill:#4CAF50
```

### Flowmap 5: Alur Profil

```mermaid
flowchart TD
    Start([User di Home])
    ClickProfile[Klik Tab Profile]
    ProfileScreen[Layar Profil]
    
    LoadUserData[Load User Info<br/>dari Firebase Auth]
    LoadUserPosts[Load User Posts<br/>dari Firestore]
    CountStats[Hitung Statistik]
    
    DisplayProfile[Tampilkan Profil:<br/>- Avatar<br/>- Nama<br/>- Email<br/>- Jumlah Post<br/>- Badges]
    
    UserAction{Pilih Aksi}
    
    ViewMyPosts[Lihat Post Saya]
    DisplayPosts[Tampilkan List Posts]
    PostAction{Aksi Post}
    
    ViewPostDetail[Lihat Detail]
    DeletePost[Hapus Post]
    ConfirmDelete{Konfirmasi?}
    RemovePost[Hapus dari Firestore]
    
    Logout[Logout]
    ConfirmLogout{Yakin<br/>Logout?}
    ClearSession[Clear Session<br/>Firebase Auth]
    BackToAuth[Kembali ke Login]
    
    BackProfile[Kembali ke Profil]
    BackHome[Kembali ke Home]
    
    Start --> ClickProfile
    ClickProfile --> ProfileScreen
    ProfileScreen --> LoadUserData
    LoadUserData --> LoadUserPosts
    LoadUserPosts --> CountStats
    CountStats --> DisplayProfile
    
    DisplayProfile --> UserAction
    UserAction -->|Post Saya| ViewMyPosts
    UserAction -->|Logout| Logout
    UserAction -->|Kembali| BackHome
    
    ViewMyPosts --> DisplayPosts
    DisplayPosts --> PostAction
    PostAction -->|Lihat| ViewPostDetail
    PostAction -->|Hapus| DeletePost
    PostAction -->|Kembali| BackProfile
    
    ViewPostDetail --> PostAction
    
    DeletePost --> ConfirmDelete
    ConfirmDelete -->|Ya| RemovePost
    ConfirmDelete -->|Tidak| DisplayPosts
    RemovePost --> DisplayPosts
    
    Logout --> ConfirmLogout
    ConfirmLogout -->|Ya| ClearSession
    ConfirmLogout -->|Tidak| DisplayProfile
    ClearSession --> BackToAuth
    
    BackProfile --> DisplayProfile
    BackHome --> Start
    
    style Start fill:#4CAF50
    style BackToAuth fill:#FF9800
```

---

## Arsitektur Aplikasi

### Pola Arsitektur: MVVM (Model-View-ViewModel)

```mermaid
flowchart TB
    subgraph View["View Layer"]
        Activities[Activities/<br/>Fragments]
        XML[XML Layouts]
    end
    
    subgraph ViewModel["ViewModel Layer"]
        VM1[AuthViewModel]
        VM2[ScanViewModel]
        VM3[ForumViewModel]
        VM4[HistoryViewModel]
        VM5[ProfileViewModel]
        VM6[ResultViewModel]
    end
    
    subgraph UseCase["Use Case Layer"]
        UC1[AnalyzePlantUseCase]
    end
    
    subgraph Repository["Repository Layer"]
        R1[PlantRepository]
        R2[PostRepository]
    end
    
    subgraph DataSource["Data Source Layer"]
        DS1[Room Database<br/>ScanHistoryDao]
        DS2[Firebase Auth]
        DS3[Firebase Firestore]
        DS4[Google Gemini AI]
        DS5[Cloudinary API]
        DS6[Local Storage]
    end
    
    subgraph DI["Dependency Injection"]
        Hilt[Hilt/Dagger]
    end
    
    Activities --> VM1
    Activities --> VM2
    Activities --> VM3
    Activities --> VM4
    Activities --> VM5
    Activities --> VM6
    
    VM2 --> UC1
    UC1 --> R1
    
    VM3 --> R2
    VM4 --> R1
    
    R1 --> DS1
    R1 --> DS4
    R1 --> DS6
    
    R2 --> DS3
    R2 --> DS5
    
    VM1 --> DS2
    VM5 --> DS2
    VM5 --> DS3
    
    Hilt -.-> VM1
    Hilt -.-> VM2
    Hilt -.-> VM3
    Hilt -.-> VM4
    Hilt -.-> VM5
    Hilt -.-> VM6
    Hilt -.-> UC1
    Hilt -.-> R1
    Hilt -.-> R2
    
    style View fill:#E1F5FE
    style ViewModel fill:#B2EBF2
    style UseCase fill:#80DEEA
    style Repository fill:#4DD0E1
    style DataSource fill:#26C6DA
    style DI fill:#FFE082
```

---

## Teknologi Stack

### Backend Services
- **Firebase Authentication**: Autentikasi pengguna (Email/Password, Google Sign-In)
- **Firebase Firestore**: Database NoSQL untuk data forum
- **Cloudinary**: Cloud storage untuk gambar forum
- **Google Gemini AI**: AI generatif untuk analisis tanaman

### Local Storage
- **Room Database**: Database SQLite untuk riwayat scan
- **Internal Storage**: Penyimpanan file gambar lokal

### Android Framework
- **MVVM Architecture**: Pola arsitektur aplikasi
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **LiveData/Flow**: Reactive data streams
- **Navigation Component**: Navigasi antar fragment
- **CameraX**: API kamera modern
- **Coil**: Image loading library
- **Lottie**: Animasi

---

## Catatan Implementasi

1. **Data Persistence**:
   - Riwayat scan disimpan lokal menggunakan Room Database
   - Post forum disimpan di cloud menggunakan Firestore
   - Gambar scan disimpan di internal storage
   - Gambar forum di-upload ke Cloudinary

2. **Authentication Flow**:
   - Mendukung login dengan email/password
   - Mendukung Google Sign-In
   - Session dikelola oleh Firebase Auth

3. **AI Integration**:
   - Menggunakan Google Gemini 1.5 Flash model
   - Prompt terstruktur untuk hasil konsisten
   - Retry mechanism untuk menangani rate limiting

4. **Forum Features**:
   - Real-time updates menggunakan Firestore listeners
   - Like system dengan array manipulation
   - User dapat menghapus post sendiri

5. **Offline Support**:
   - Riwayat scan tersimpan lokal
   - Dapat dilihat tanpa koneksi internet
   - Forum memerlukan koneksi internet

---

**Dokumen ini dibuat berdasarkan analisis kode sumber HerbaMed Jabar**  
**Versi: 1.0**  
**Tanggal: 2025-12-15**
