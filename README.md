## Text Manipulator (TEMA)


#### ⏳ A Kotlin/Native sandbox

![Build Status](https://img.shields.io/github/workflow/status/milosmns/tema/Build%20Release?label=Build&logo=github)
![Code Size](https://img.shields.io/github/languages/code-size/milosmns/tema?label=Code%20Size&logo=github&style=flat-square)
![License](https://img.shields.io/github/license/milosmns/tema?label=License)

Text Manipulator (TEMA) helps you... well, manipulate text. 😃

Its main purpose is to serve as a working Kotlin/Native showcase app.

TEMA supports some basic operations like

- Reversing text
    - `"I want an apple."` becomes `".elppa na tnaw I"`
- Flipping text
    - `"I want an apple."` becomes `"I ʍɐuʇ ɐu ɐddlǝ˙"`
- and so on...

To use the tool and see all of its options, you need a TEMA binary on your device.
There's a bunch of binaries [in the releases section](https://github.com/milosmns/tema/releases), 
but you can also build TEMA on your own.


### 🛠️ Building

```bash
git clone <this repo>
cd tema
./gradlew build
```

That's pretty much it. Kotlin/Native libs will be pulled, and your tool will be in `build/bin/native/`, built for your OS.


### ▶️ Running TEMA

TEMA can be run from the command line.

```bash
tema <operation> [modifiers] <content>
```

For help, simply run

```bash
tema --help
```


### 💬 Contributing

Feel free to open issues, pull requests, propose suggestions, ask questions, etc.


### ⚖️ License

MIT. See the license in [the license file](https://github.com/milosmns/tema/blob/master/LICENSE).
