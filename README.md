#  AI Resume Tailor - Backend API

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green)
![Gemini AI](https://img.shields.io/badge/AI-Google_Gemini-blue)

**AI Resume Tailor** is an intelligent engine designed to surgically optimize resumes for specific Job Descriptions (JDs). Unlike generic tools, it uses **Generative AI** to rewrite the *Professional Summary* and *Bullet Points* using the **STAR Method**, ensuring a 95%+ ATS Score without hallucinating dates or companies.

---

## 🏗️ Architecture & Flow

1.  **Extraction Layer:** Uses `Apache PDFBox` to extract raw text from the uploaded PDF.
2.  **Structural Parsing:** AI converts unstructured text into a structured Java Object (`ResumeData`).
3.  **Surgical Optimization:**
    * **Engine:** Google Gemini 1.5 Flash/Pro.
    * **Logic:** Rewrites experience bullets to be result-oriented (STAR Method).
    * **Constraint:** Keeps Company Names, Dates, and Titles strictly unchanged.
4.  **PDF Generation:** Uses `Thymeleaf` templates + `OpenHTMLToPDF` to generate a pixel-perfect, ATS-friendly PDF.

---

## 🛠️ Tech Stack

* **Core:** Java 17, Spring Boot 3.3
* **AI Integration:** Google Gemini API (REST Client)
* **PDF Processing:** Apache PDFBox, OpenHTMLToPDF
* **Templating:** Thymeleaf
* **Build Tool:** Maven

---

## ⚙️ Setup & Installation

### 1. Clone the Repository
```bash
git clone [https://github.com/Aliurooz786/ai-resume-tailor-backend.git](https://github.com/Aliurooz786/ai-resume-tailor-backend.git)
cd ai-resume-tailor-backend
