# DailyPulse — Edição de Portfólio em KMP

**CI (Codemagic):** O pipeline [`kmp-workflow`](./codemagic.yaml) (`KMP Build & Test Lab`) roda em pushes para `main`. [Ver builds →](https://codemagic.io/app/69f8c24fe73167699549e9f5)

[Read this in English →](./README.md)

> Um leitor de notícias que demonstra **duas formas de construir UI em cima do mesmo núcleo de negócio em Kotlin Multiplatform**:
> 1. **Compose Multiplatform** — uma única árvore de UI Kotlin/Compose entregue para Android, iOS, Desktop e Web.
> 2. **UI Nativa** — Jetpack Compose no Android e SwiftUI no iOS, cada uma consumindo as mesmas ViewModels compartilhadas.

O objetivo desta versão **não é ensinar KMP do zero**; é **mostrar, em um único repositório, os trade-offs arquiteturais entre compartilhar a UI e mantê-la nativa**, reaproveitando 100% da lógica de negócio.

---

## Cursos (Udemy)

O repositório de exercícios **DailyPulse** original e os branches progressivos são do **Petros Efthymiou**. Os cursos abaixo são **pagos** na Udemy:

1. [**Kotlin Multiplatform Masterclass — KMP, KMM — Android, iOS**](https://www.udemy.com/course/kotlin-multiplatform-masterclass/)
2. [**Full-stack Compose Multiplatform Masterclass — KMP**](https://www.udemy.com/course/fullstack-compose-multiplatform-masterclass-kmp/)

Código upstream: [github.com/petros-efthymiou/DailyPulse](https://github.com/petros-efthymiou/DailyPulse). Este fork acrescenta os flavors Android `native` / `mpp`, o switch de UI no iOS e documentação voltada ao portfólio.

---

## Sumário

- [Cursos (Udemy)](#cursos-udemy)
- [O que é compartilhado, o que é por flavor](#o-que-é-compartilhado-o-que-é-por-flavor)
- [Build flavors em um relance](#build-flavors-em-um-relance)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitetura](#arquitetura)
- [Como executar](#como-executar)
  - [Android — `mpp` (Compose Multiplatform)](#android--mpp-compose-multiplatform)
  - [Android — `native` (Jetpack Compose)](#android--native-jetpack-compose)
  - [iOS — `mpp` (Compose Multiplatform)](#ios--mpp-compose-multiplatform)
  - [iOS — `native` (SwiftUI)](#ios--native-swiftui)
- [Como o switch de flavor funciona](#como-o-switch-de-flavor-funciona)
- [Verificando dependências do Gradle](#verificando-dependências-do-gradle)
- [Branches do curso (referência)](#branches-do-curso-referência)
- [Autor](#autor)
- [Licença](#licença)

---

## O que é compartilhado, o que é por flavor

```text
┌─────────────────────────────────────────────────────────────────┐
│   :shared (Kotlin Multiplatform)                                │
│                                                                 │
│   commonMain                                                    │
│     ├── articles/  sources/   (UseCases, Repositories, DTOs)    │
│     ├── presentation/         (ArticlesViewModel, *State)       │
│     ├── di/                   (Módulos Koin)                    │
│     ├── db/                   (Schema SQLDelight)               │
│     └── ui/                   (Telas Compose Multiplatform)     │
│                               (consumido só pelo flavor mpp)    │
│                                                                 │
│   androidMain   iosMain   (Engine Ktor, driver SQL, Platform)   │
└─────────────────────────────────────────────────────────────────┘
                              ▲                ▲
                              │                │
                ┌─────────────┘                └────────────┐
                │                                           │
   ┌────────────┴──────────┐                  ┌─────────────┴───────────┐
   │  androidApp           │                  │  iosApp                 │
   │  ┌────────┐ ┌───────┐ │                  │  ┌─────────┐ ┌────────┐ │
   │  │ src/   │ │ src/  │ │                  │  │ Content │ │ Native │ │
   │  │ mpp/   │ │native/│ │                  │  │ View    │ │ Root   │ │
   │  │  └ App │ │ └ JC  │ │                  │  │ (CMP)   │ │ View   │ │
   │  └────────┘ └───────┘ │                  │  └─────────┘ └────────┘ │
   └───────────────────────┘                  └─────────────────────────┘
        Android Product Flavors                  Flag de compilação Swift
        (mpp / native)                           (-D MPP_UI)
```

Tudo **da ViewModel para baixo** vive em `:shared/commonMain` e é reaproveitado pelos dois flavors. As duas variantes de UI só diferem em *como o mesmo `ArticlesViewModel.articlesState` é renderizado*.

---

## Build flavors em um relance

| Flavor | UI Android | UI iOS | Carregamento de imagens | Navegação | Application ID |
|--------|------------|--------|--------------------------|-----------|----------------|
| `mpp`    | `App()` em Compose Multiplatform vindo de `:shared` | `MainViewController()` de `:shared` (envelopado em `UIViewControllerRepresentable`) | Kamel | Voyager | `…dailypulse.android.mpp` |
| `native` | Jetpack Compose escrito em `androidApp/src/native/` | Telas SwiftUI em `iosApp/iosApp/Screens/` | Coil (Android), `AsyncImage` (iOS) | `androidx.navigation.compose` (Android), `NavigationStack` (iOS) | `…dailypulse.android.native` |

Os dois flavors do Android instalam lado a lado porque possuem application IDs distintos.

---

## Estrutura do projeto

```text
DailyPulse/
├── shared/                                  # Módulo Kotlin Multiplatform
│   └── src/
│       ├── commonMain/kotlin/.../
│       │   ├── articles/                    # lógica de negócio (use cases, repo, VM)
│       │   ├── sources/
│       │   ├── di/                          # Módulos Koin (compartilhados)
│       │   ├── db/                          # Banco SQLDelight
│       │   └── ui/                          # Telas Compose Multiplatform (só mpp)
│       ├── androidMain/                     # Engine Ktor Android, driver SQL
│       └── iosMain/                         # Engine Ktor Darwin, driver SQL,
│                                            # KoinInitializer, MainViewController
│
├── androidApp/
│   └── src/
│       ├── main/                            # AndroidManifest, Application,
│       │                                    # módulos Koin compartilhados
│       ├── mpp/java/.../                    # ⇨ MainActivity que hospeda App() do shared
│       └── native/java/.../                 # ⇨ MainActivity + telas Jetpack Compose
│
├── iosApp/
│   └── iosApp/
│       ├── iOSApp.swift                     # Faz o switch via #if MPP_UI
│       ├── ContentView.swift                # Entrada MPP (UIViewControllerRepresentable)
│       ├── NativeRootView.swift             # Entrada nativa (NavigationStack)
│       └── Screens/                         # Telas SwiftUI (consumidas pelo native)
│
└── gradle/libs.versions.toml                # Fonte única da verdade para versões
```

---

## Stack tecnológico

| Camada | Biblioteca |
|--------|------------|
| Linguagem | Kotlin 1.9.22, Swift 5 |
| Async | kotlinx.coroutines, kotlinx.datetime |
| Networking | Ktor 2.3 (engine Android + engine Darwin) |
| Persistência | SQLDelight 2.0 |
| Injeção de dependências | Koin 3.6 (`koin-core`, `koin-android`, `koin-compose`, `koin-androidx-compose`) |
| UI — flavor `mpp` | Compose Multiplatform 1.6.1, Voyager 1.1, Kamel |
| UI — flavor `native` (Android) | Jetpack Compose (Material 3 1.2.1), `androidx.navigation.compose`, Coil |
| UI — flavor `native` (iOS) | SwiftUI, `NavigationStack`, `AsyncImage` |

---

## Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│  UI (Compose Multiplatform │ Jetpack Compose │ SwiftUI)     │  ← por flavor
├─────────────────────────────────────────────────────────────┤
│  Apresentação — ArticlesViewModel, SourcesViewModel          │  ── compartilhado
│  Aplicação    — UseCases, modelos de domínio                 │  ── compartilhado
│  Dados        — Repositórios, serviço Ktor, SQLDelight       │  ── compartilhado
│  Infra        — Platform, drivers de DB, engine Ktor         │  ── por plataforma
└─────────────────────────────────────────────────────────────┘
```

O padrão é **Clean Architecture + estado no estilo MVI**, com um único `StateFlow<XxxState>` por tela. Pull-to-refresh, tratamento de erro e estados de loading vivem no código compartilhado.

---

## Como executar

### Pré-requisitos

- JDK 17
- Android Studio Hedgehog (ou superior) com Android SDK 34
- Xcode 15+ (para os alvos iOS)
- Um `local.properties` com `sdk.dir=…`
- Uma chave da News API (o projeto vem com a do curso; substitua se desejar)

### Android — `mpp` (Compose Multiplatform)

```bash
./gradlew :androidApp:assembleMppDebug
./gradlew :androidApp:installMppDebug         # instala em um device conectado
```

Ou, no Android Studio:

1. Abra a janela Build Variants (`View → Tool Windows → Build Variants`).
2. Para o módulo `androidApp`, escolha a variant **`mppDebug`**.
3. Rode a configuração `androidApp`.

O application ID deste flavor é `com.petros.efthymiou.dailypulse.android.mpp`, então ele convive com o flavor nativo no mesmo device.

### Android — `native` (Jetpack Compose)

```bash
./gradlew :androidApp:assembleNativeDebug
./gradlew :androidApp:installNativeDebug
```

Ou escolha a variant **`nativeDebug`** no painel Build Variants.

Esse flavor usa `androidx.compose.material3`, `androidx.navigation.compose`, Coil e `koin-androidx-compose`. Ele **não** depende de Compose Multiplatform, Voyager ou Kamel.

### iOS — `mpp` (Compose Multiplatform)

O framework iOS é construído pelo Gradle e consumido pelo Xcode:

```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

Em seguida, no Xcode:

1. Abra `iosApp/iosApp.xcodeproj`.
2. Selecione o target `iosApp` → **Build Settings** → **Other Swift Flags**.
3. Adicione `-D MPP_UI` para as configurações *Debug* e *Release* do **scheme MPP** (veja abaixo).
4. Rode o scheme `iosApp`.

`iOSApp.swift` vai escolher `ContentView()`, que envelopa o `MainViewController()` Kotlin — exatamente a mesma árvore Compose Multiplatform que roda no Android.

### iOS — `native` (SwiftUI)

1. Abra `iosApp/iosApp.xcodeproj`.
2. Garanta que `MPP_UI` **não** esteja definido em *Other Swift Flags* (esse é o padrão).
3. Rode o scheme `iosApp`.

`iOSApp.swift` vai escolher `NativeRootView()`, que renderiza as telas SwiftUI em `iosApp/iosApp/Screens/`. Elas consomem `ArticlesViewModel`, `SourcesViewModel` e `Platform` direto do framework compartilhado, via os helpers `*Injector` (Koin).

> **Setup recomendado no Xcode**: duplique o scheme padrão `iosApp` em dois — `iosApp-MPP` e `iosApp-Native`. Adicione `-D MPP_UI` em *Other Swift Flags* só no `iosApp-MPP`. A partir daí, alternar de flavor no iOS é um clique.

---

## Como o switch de flavor funciona

### Android (Gradle product flavors)

```kotlin
// androidApp/build.gradle.kts
android {
    flavorDimensions += "ui"
    productFlavors {
        create("mpp") {
            dimension = "ui"
            applicationIdSuffix = ".mpp"
            buildConfigField("String", "FLAVOR_LABEL", "\"Compose Multiplatform\"")
        }
        create("native") {
            dimension = "ui"
            applicationIdSuffix = ".native"
            buildConfigField("String", "FLAVOR_LABEL", "\"Jetpack Compose (Native)\"")
        }
    }
}

dependencies {
    "mppImplementation"(libs.koin.compose)
    "nativeImplementation"(libs.androidx.navigation.compose)
    "nativeImplementation"(libs.coil.compose)
    "nativeImplementation"(libs.koin.androidx.compose)
    "nativeImplementation"(libs.androidx.compose.material) // pull-to-refresh
}
```

A `MainActivity` **não** fica em `src/main/`. Ela existe uma vez em `src/mpp/java/…` (delegando para `App()` do `:shared`) e uma vez em `src/native/java/…` (montando um grafo `androidx.navigation.compose`). O `AndroidManifest.xml` e a Application class `DailyPulseApp` ficam em `src/main/` e são compartilhados.

### iOS (flag de compilação Swift)

```swift
// iosApp/iosApp/iOSApp.swift
@main
struct iOSApp: App {
    init() { KoinInitializerKt.doInitKoin() }
    var body: some Scene {
        WindowGroup {
            #if MPP_UI
            ContentView()        // Compose Multiplatform
            #else
            NativeRootView()     // SwiftUI
            #endif
        }
    }
}
```

A entrada MPP é o `ContentView` original que envelopa `MainIOSKt.MainViewController()`. A entrada nativa é o novo `NativeRootView`, que coloca as telas SwiftUI existentes (`ArticlesScreen`, `SourcesScreen`, `AboutScreen`) dentro de um `NavigationStack`.

---

## Verificando dependências do Gradle

O catálogo `gradle/libs.versions.toml` é a fonte única da verdade. As adições relevantes para esta versão de portfólio são:

| Chave | Usado por | Propósito |
|-------|-----------|-----------|
| `androidx-navigation-compose` | `nativeImplementation` | Navegação no flavor nativo |
| `coil-compose` | `nativeImplementation` | Carregamento de imagens no flavor nativo |
| `koin-androidx-compose` | `nativeImplementation` | `koinViewModel()` em `@Composable`s |
| `androidx-compose-material` | `nativeImplementation` | APIs `pullrefresh` no flavor nativo |
| `koin-compose` | `mppImplementation` | `koinInject()` dentro de Compose Multiplatform |
| `compose.runtime/foundation/material3`, `voyager-*`, `kamel-image` | `:shared/commonMain` | UI Compose Multiplatform consumida pelo flavor mpp |

Para provar que a matriz compila ponta a ponta:

```bash
./gradlew :androidApp:assembleMppDebug :androidApp:assembleNativeDebug
```

Você deve ver dois APKs distintos:

```
androidApp/build/outputs/apk/mpp/debug/androidApp-mpp-debug.apk
androidApp/build/outputs/apk/native/debug/androidApp-native-debug.apk
```

### Testes instrumentados Android / Firebase Test Lab

Para gerar o app **mpp** debug e o APK de testes instrumentados (o mesmo par usado no `codemagic.yaml` para o Firebase Test Lab):

```bash
./gradlew :androidApp:assembleMppDebug :androidApp:assembleMppDebugAndroidTest
```

Com dispositivo ou emulador conectado:

```bash
./gradlew :androidApp:connectedMppDebugAndroidTest
```

Para `gcloud firebase test android run --type instrumentation`, o APK do **app** fica em `outputs/apk/<flavor>/debug/`, mas o APK de **teste** é gerado em `outputs/apk/androidTest/...` (não ao lado do app):

```
androidApp/build/outputs/apk/mpp/debug/androidApp-mpp-debug.apk
androidApp/build/outputs/apk/androidTest/mpp/debug/androidApp-mpp-debug-androidTest.apk
```

No flavor **native**, use `assembleNativeDebug` / `assembleNativeDebugAndroidTest` (ou `connectedNativeDebugAndroidTest`) e troque `mpp` por `native` nos dois caminhos.

---

## Branches do curso (referência)

Esta versão de portfólio foi construída em cima das branches originais do curso. Elas são preservadas para referência:

| Branch | Tópico |
|--------|--------|
| `1_initial` | Esqueleto do projeto |
| `2_about_screen` | Primeira tela compartilhada (About / Platform info) |
| `3_articles_presentation_logic_and_UI` | Pipeline MVI dos artigos |
| `4_articles_networking_and_business_logic` | Ktor + repositório |
| `5_dependency_injection_with_koin` | Módulos Koin |
| `6_local_database_with_sql-delight` | SQLDelight + pull-to-refresh |
| `7_final_sources_feature` | UIs nativas (Jetpack Compose + SwiftUI) |
| `8_compose_android_iOS` | **Compose Multiplatform no Android + iOS (base desta branch)** |
| `9_compose_desktop` | Target Desktop (CMP) |
| `10_compose_web` | Target Web (CMP / Wasm) |

---

## Autor

Fork de portfólio mantido por **[Cristiano Cortez](https://www.linkedin.com/in/cristianocortez/)**.

---

## Licença

```
Copyright (C) 2023 Petros Efthymiou Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
