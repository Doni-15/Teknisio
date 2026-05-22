# Penjelasan Arsitektur MVVM untuk Mobile App

## Struktur Dasar MVVM

```text
View → ViewModel → Model → Database
```

Arsitektur ini digunakan supaya kode aplikasi lebih rapi, mudah dikembangkan, dan mudah di-maintain oleh tim.

---

# 1. View

Bagian yang dilihat dan digunakan user.

Contoh:

* Halaman Login
* Dashboard
* Form Registrasi
* Tombol
* List Data

Tugas View:

* Menampilkan data ke user
* Mengirim aksi user ke ViewModel
* Tidak boleh langsung akses database

Contoh:

```text
User tekan tombol Login
↓
View mengirim email & password ke ViewModel
```

---

# 2. ViewModel

Penghubung antara View dan Model.

ViewModel menerima input dari View lalu memprosesnya menggunakan Model.

Tugas ViewModel:

* Mengatur logic tampilan
* Menyimpan state UI
* Validasi input sederhana
* Menghubungkan View dengan Model

ViewModel tidak berhubungan langsung dengan database.

Contoh:

```text
ViewModel menerima email & password
↓
ViewModel meminta Model melakukan login
↓
Hasil dikirim kembali ke View
```

---

# 3. Model

Bagian inti pengolahan data dan business logic.

Di layer Model biasanya terdapat:

* Repository
* Service
* Entity/Data Class
* API Handler
* Database Access

Tugas Model:

* Mengambil data dari database
* Mengirim data ke server API
* Menyimpan data
* Mengatur business logic aplikasi

Contoh:

```text
Model mengecek data user di database
↓
Jika cocok maka kirim hasil login berhasil
```

---

# 4. Database

Tempat penyimpanan data aplikasi.

* PostgreSQL

Database hanya diakses oleh Model.

---

# Alur Kerja MVVM

## Contoh Login

```text
User Input Email & Password
↓
View
↓
ViewModel
↓
Model
↓
Database/API
↓
Model
↓
ViewModel
↓
View
↓
Hasil ditampilkan ke user
```

---

# Pembagian Tugas Tim

## Frontend Mobile

Fokus di:

* View
* UI/UX
* Tampilan aplikasi

## ViewModel Developer

Fokus di:

* State management
* Logic tampilan
* Penghubung data

## Backend/Data Developer

Fokus di:

* Model
* Repository
* API
* Database
