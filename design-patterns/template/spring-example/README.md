# `spring-example/` — optional Spring wiring

Some patterns map naturally to Spring (beans, `ApplicationContext`, `List` of implementations). Use this folder only when it **clarifies** the pattern; skip it for purely domain or console examples.

Keep it:

- **Small** — one feature slice, not a full Boot app unless the repo already standardizes on one.
- **Consistent** with packaging and style used elsewhere in the workshop.

If you do not add Spring code, remove this folder.
