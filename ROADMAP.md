# 🗺️ ROADMAP - CraftEngineConverter

> **Last Updated:** January 1, 2026
> **Project Status:** 🟢 Active Development
> **This file serves as the main project roadmap and development tracker.**

---

## 🚀 Roadmap

### Version 1.0.0 ()
- [ ] Complete Nexo conversion (items, glyphs, emojis, images, languages, sounds, equipment, furniture, custom blocks, mechanics)
- [ ] Replace Nexo block/ furniture with their CraftEngine equivalents
- [ ] Full security audit passed
- [ ] 80%+ test coverage
- [ ] Documentation complete

### Version 1.1.0 (ItemsAdder Support)
- [ ] Items, blocks converter
- [ ] Resource pack migration
- [ ] Documentation & examples
 
### Version 1.2.0 (Oraxen Support)
- [ ] Items, blocks, furniture converter
- [ ] Resource pack migration
- [ ] Documentation & examples

---

## 🛡️ Security & Quality
- [x] Fix NoSuchFileException during ZIP extraction
- [x] Zip Slip vulnerability protection (CWE-22)
- [x] URL decoding validation (`..%2F..%2F`)
- [x] Block UNC paths (`\\server\share`)
- [x] Add comprehensive security tests
- [x] Fix all `mkdirs()`/`delete()` ignored warnings
- [x] Add `try-with-resources` for SnakeUtils
- [x] Refactor duplicate code in armor conversion

---

## 🧪 Testing
- [x] Security tests (Zip Traversal)
- [ ] SnakeUtils tests (full coverage)
- [ ] ConfigPath tests
- [ ] Converter tests (each type)
- [ ] Integration: Nexo pipeline, resource pack, multi-threading, Folia compatibility
- [ ] Manual: Real Nexo/Oraxen/ItemsAdder packs, performance benchmarks

---

## 📚 Documentation
- [x] README.md
- [x] CONTRIBUTING.md
- [x] SECURITY_TESTING.md
- [x] [Wiki pages](https://1robie.gitbook.io/craftengineconverter)
- [ ] Migration guides
- [ ] FAQ section
- [ ] API documentation & code examples
- [ ] Tag processor & extension guide

---

## 🎨 Features & Enhancements
- [x] Glyph tag processor
- [x] PlaceholderAPI tag processor
- [ ] Custom tag creation API
- [ ] Tag validation and sanitization
- [ ] Partial conversion (select items)
- [x] Dry-run mode
- [ ] Backup/rollback system
- [ ] Conversion profiles (save/load)
- [ ] Better console output (colors, formatting)
- [x] Progress bars for long operations
- [ ] Optimize async conversion (thread pools)
- [ ] Progress tracking for large conversions
- [ ] Cache frequently accessed configs
- [ ] Batch file operations

---

## 🐛 Known Issues



---

## 🔄 DevOps & Community
- [ ] GitHub Actions (CI)
- [ ] Automated PR testing
- [ ] Code quality (SonarQube)
- [ ] Security scanning (Dependabot)
- [ ] Automatic releases
- [ ] Auto-publish: Maven Central, SpigotMC, Modrinth
- [ ] GitHub Discussions, issue/PR templates
- [ ] Publish on SpigotMC, Modrinth

---

## ✅ Recently Completed

- ItemsAdder basic furniture conversion
- ItemsAdder pack conversion
- ItemsAdder recipe conversion
- ItemsAdder song conversion
- ItemsAdder font conversion (no emojis yet)
- Multiple Translation support (English, French) + easy addition of new languages
- Nexo Recipes conversion
- Interaction with furniture/block convert to CE equivalent
- Fix Nexo JukeboxPlayableComponent && PlaceOn/Break
- Multi-threaded pack conversion: just add the `--threads=<number>` argument to the command line
- Dry-run mode for converters just need to add `--dryrun` argument to the command line
- Configuration for progress bar display for converters
- Progress bars for long operations
- Fixed NoSuchFileException in ZIP extraction
- Added URL decoding validation (..%2F)
- Added UNC path blocking (\\server\share)
- Created comprehensive security tests
- Updated README, CONTRIBUTING, SECURITY_TESTING
- Implemented Zip Slip protection
- Created SnakeUtils utility
- Tag processor system
- Multi-language support
- Equipment conversion (Component & Trim)
- Folia compatibility

---

**Legend:**
- 🚀 Roadmap | 🛡️ Security | 🧪 Testing | 📚 Docs | 🎨 Features | 🐛 Bug | 🔄 DevOps | ✅ Done

*For contribution guidelines, see [CONTRIBUTING.md](CONTRIBUTING.md)*
