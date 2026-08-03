# Banquerito 🏦

**Your finances, your rules — banking clarity for expats and freelancers across borders.**

Banquerito is a Kotlin Multiplatform app that helps expats, freelancers, and *autónomos* manage multi-currency accounts, plan payments, calculate real tax obligations, and chat with an AI financial consultant that understands their full financial context — all stored locally, on-device.

Built as a portfolio project targeting fintech roles in Spain and across Europe.

---

## ✨ Features

### 💳 Accounts
- Multiple accounts across different currencies, with live exchange rates
- Total balance converted to your preferred display currency
- Full transaction history with income / expense / transfer tracking and filtering
- SIM card payment reminders per account

### 📅 Planning
- Recurring and one-off planned payments and expected income
- "Do I have enough on this account?" indicator for upcoming obligations
- Archive for completed or inactive items

### 🧾 Taxes
- Country-aware tax calculator (currently: Spain, Serbia, Armenia)
- Adapts to your specific employment status — Autónomo, Digital Nomad, Employee, dependent visa holder, etc.
- Interactive income slider to model "what if I earn X this quarter"
- Visual breakdown: net income vs. IRPF reserve vs. IVA vs. cuota autónomo
- Upcoming tax deadlines (Modelo 130/131, Renta, and more)
- Choose which accounts count as taxable income

### 🤖 AI Consultant
- Chat with a Claude-powered assistant that has full context of your accounts, tax profile, and balances
- Attach PDFs, images, Word/Excel/RTF/CSV/TXT files — text is extracted **on-device**, no third-party document parsing service
- Persistent chat history that survives app restarts
- Automatic conversation summarization once history grows large, keeping full history visible to you while staying within the model's context window

### ⚙️ Settings
- Personal profile: name, country of residence, citizenship, default currency
- Country-specific tax profile with dynamic fields per jurisdiction, stored as versioned JSON — new countries can be added without a database migration

---

## 🏗️ Architecture & Tech Stack

| Layer | Technology |
|---|---|
| UI | [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) |
| Architecture | MVVM + Clean Architecture (repositories behind interfaces) |
| Local database | [SQLDelight](https://cashapp.github.io/sqldelight/) |
| Dependency Injection | [Koin](https://insert-koin.io/) |
| Networking | [Ktor](https://ktor.io/) |
| Serialization | kotlinx.serialization |
| Date/Time | kotlinx.datetime |
| AI | [Claude API](https://www.anthropic.com/) (Anthropic) |
| Testing | kotlin.test, JUnit, kotlinx-coroutines-test |

### Module structure

```
shared/                    — all business logic, shared between Android and iOS
├── commonMain/             — models, repositories, ViewModels, UI screens, calculators
├── androidMain/             — expect/actual implementations (file storage, DB driver)
└── iosMain/                 — expect/actual implementations (in progress)

androidBanquerito/          — Android app entry point (MainActivity, DI wiring, resources)
iosBanquerito/               — iOS app entry point (in progress)
```

### Design principles

- **Repository pattern behind interfaces** (`IAccountRepository`, `ISettingsRepository`, `IPlannedPaymentRepository`, `IExchangeRateRepository`, `IChatRepository`, `IClaudeApi`) — every repository is injected via Koin as an interface, making ViewModels fully testable against fakes instead of a real database.
- **expect/actual** for everything platform-specific: file storage, file opening, database driver, on-device document text extraction.
- **Pure calculators** — the tax engine (`TaxCalculator`) has no side effects, taking an income figure and a tax profile and returning a breakdown. This makes it trivial to unit test exhaustively.
- **Local-first, privacy-first** — all financial data lives in an on-device SQLite database. There is no backend, no user accounts, and no cloud sync. The only data that leaves the device is what you explicitly send to the AI Consultant, plus anonymous exchange-rate lookups.

---

## 🧪 Testing

66+ passing unit tests covering the tax calculator and every ViewModel's business logic:

| Test class | Tests |
|---|---|
| `TaxCalculatorTest` | 21 |
| `AccountsViewModelTest` | 17 |
| `PlannedPaymentViewModelTest` | 12 |
| `SettingsViewModelTest` | 6 |
| `TaxesViewModelTest` | 10 |

Fake repository implementations live in `shared/src/androidUnitTest/kotlin/.../testutil/`.

Run all tests:

```bash
./gradlew :shared:testDebugUnitTest
```

Test report: `shared/build/reports/tests/testDebugUnitTest/index.html`

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 17+
- An Anthropic API key ([console.anthropic.com](https://console.anthropic.com))

### Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/SleeplessShu/Banquerito.git
   cd Banquerito
   ```

2. Create a `secrets.properties` file in the project root:
   ```properties
   ANTHROPIC_API_KEY=sk-ant-your-key-here
   ```

3. Open the project in Android Studio and let Gradle sync.

4. Run the `androidBanquerito` configuration on an emulator or device.

---

## 🗺️ Roadmap

- [ ] Progressive IRPF tax brackets with multiple income-extrapolation modes
- [ ] Push notifications for payment and tax deadlines (WorkManager)
- [ ] Open Banking integration (Plaid) for automatic transaction import
- [ ] Full iOS build
- [ ] CI/CD with automated test runs on every push

---

## 📄 Privacy

Banquerito stores all financial data locally on your device. There is no backend server and no user account system. See [`PRIVACY.md`](./privacy_policy.html) for full details on how data is handled, including third-party services (Anthropic Claude API, exchange rate API).

---

## 👤 Author

Built by [Dmitry](https://github.com/SleeplessShu) — Android/Kotlin developer based in Barcelona.

---

## 📝 License

This project is currently unlicensed / all rights reserved. 
