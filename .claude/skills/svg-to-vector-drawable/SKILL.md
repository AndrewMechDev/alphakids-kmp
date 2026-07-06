---
name: svg-to-vector-drawable
description: "Trigger: SVG icon, vector drawable, XML drawable, Android icon conversion. Convert SVG icons to Android Vector Drawable XML for Compose Multiplatform."
license: Apache-2.0
metadata:
  author: "AndrewMechDev"
  version: "1.0"
---

## Activation Contract

Activate when:
- Adding a new SVG icon to `composeResources/drawable/`
- Android crashes with "Android platform doesn't support SVG format"
- User asks to convert SVG to vector drawable or XML drawable

## Hard Rules

- Android does NOT support SVG in `painterResource()`. Always use `.xml` vector drawables.
- Output file extension MUST be `.xml`, never `.svg`.
- Root element MUST be `<vector>` with `xmlns:android="http://schemas.android.com/apk/res/android"`.
- Use `android:width` and `android:height` in `dp`, matching the SVG logical size.
- `android:viewportWidth` and `android:viewportHeight` map from SVG `viewBox` dimensions.
- Transparent fill: use `android:fillColor="#00000000"`, never `fill="none"`.
- Kotlin resource references (`Res.drawable.ic_xxx`) work with both `.svg` and `.xml` — no code changes needed.

## Decision Gates

| SVG Element | Vector Drawable Equivalent |
|---|---|
| `viewBox="0 0 W H"` | `android:viewportWidth="W"` + `android:viewportHeight="H"` |
| `<path d="...">` | `<path android:pathData="...">` |
| `stroke="#color"` | `android:strokeColor="#color"` |
| `stroke-width="N"` | `android:strokeWidth="N"` |
| `fill="#color"` | `android:fillColor="#color"` |
| `fill="none"` | `android:fillColor="#00000000"` |
| `stroke-linecap` | `android:strokeLineCap` (camelCase) |
| `stroke-linejoin` | `android:strokeLineJoin` (camelCase) |
| `<circle cx="X" cy="Y" r="R">` | `<path>` with arc: `M X-R,Y a R,R 0 1,0 2R,0 a R,R 0 1,0 -2R,0` |
| `<line x1 y1 x2 y2>` | `<path android:pathData="M x1,y1 L x2,y2">` |
| `<rect x y w h>` | `<path android:pathData="M x,y h w v h h -w Z">` |
| `<g>` with transform | Flatten transforms into path data or use `<group>` |

## Execution Steps

1. Read the SVG file. Extract `viewBox` width/height and all visual elements.
2. Create the XML vector drawable. See `references/conversion-guide.md` for the template.
3. Map each SVG element to its Android equivalent using the decision gate table above.
4. Save as `.xml` in the same `drawable/` directory with the same base name.
5. Delete the original `.svg` file.
6. Verify the build compiles: `./gradlew :androidApp:assembleDebug`.

## Output Contract

Return:
- Files created (`.xml`) and deleted (`.svg`).
- Any elements that required manual conversion (complex gradients, filters, masks).
- Build verification result.

## References

- `references/conversion-guide.md` — template, examples, and edge cases.
