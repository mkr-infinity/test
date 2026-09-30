#!/usr/bin/env python3
"""Render original Auto Optimiser rocket artwork, never the supplied reference bitmap.

Only stdlib is needed for vectors. Run rsvg-convert separately for PNG derivatives.
Coordinates share one 108-unit canvas; the mark fits Android's central 66-unit circle.
"""
from pathlib import Path
from xml.sax.saxutils import escape

ROOT = Path(__file__).resolve().parents[1]
# The open orbit, compact body and short flame are intentionally separate flat shapes.
PATHS = [
    ("M42,28 C30,33 24,43 24,54 C24,66 31,75 42,80", None, "#1675D1", 3.5),
    ("M66,28 C78,33 84,43 84,54 C84,66 77,75 66,80", None, "#11B6BD", 3.5),
    ("M42,53 C36,56 33,63 34,70 L45,63 Z", "#1761BD", None, 0),
    ("M66,53 C72,56 75,63 74,70 L63,63 Z", "#1761BD", None, 0),
    ("M54,23 C44,31 40,44 42,56 L46,66 Q54,62 62,66 L66,56 C68,44 64,31 54,23 Z M54,38 A5,5 0,1 0,54,48 A5,5 0,1 0,54,38 Z", "#087ED5", None, 0),
    ("M65.5,49 C66,54 64.5,60 62,66 Q58,64 54,64 L47,65 Z", "#19C5D2", None, 0),
    ("M54,69 C49,69 47,73 49,77 Q51,82 54,85 Q57,82 59,77 C61,73 59,69 54,69 Z", "#F2A12C", None, 0),
]


def svg(background=True):
    elements = ['<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 108 108" role="img" aria-labelledby="title desc">',
                '  <title id="title">Auto Optimiser</title>',
                '  <desc id="desc">Original minimal rocket with an open blue and cyan orbit and a short amber flame.</desc>']
    if background:
        elements.append('  <rect width="108" height="108" rx="24" fill="#F8FAFC"/>')
    for data, fill, stroke, width in PATHS:
        attrs = f'fill="{fill or "none"}" fill-rule="evenodd"'
        if stroke:
            attrs += f' stroke="{stroke}" stroke-width="{width}" stroke-linecap="round"'
        elements.append(f'  <path {attrs} d="{escape(data)}"/>')
    return '\n'.join(elements + ['</svg>', ''])


def android_vector(monochrome=False):
    elements = ['<?xml version="1.0" encoding="utf-8"?>',
                '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
                '    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">']
    for index, (data, fill, stroke, width) in enumerate(PATHS):
        # The body highlight is decorative: omit it from the monochrome silhouette.
        if monochrome and index == 5:
            continue
        attrs = f'android:fillColor="{("#FFFFFFFF" if monochrome else fill) if fill else "#00000000"}" android:fillType="evenOdd"'
        if stroke:
            attrs += f' android:strokeColor="{"#FFFFFFFF" if monochrome else stroke}" android:strokeWidth="{width}" android:strokeLineCap="round"'
        elements.append(f'    <path {attrs}\n        android:pathData="{escape(data)}" />')
    return '\n'.join(elements + ['</vector>', ''])


if __name__ == '__main__':
    assets = ROOT / 'assets'
    drawable = ROOT / 'app/src/main/res/drawable'
    assets.mkdir(exist_ok=True)
    (assets / 'logo.svg').write_text(svg(), encoding='utf-8')
    (assets / 'logo-mark.svg').write_text(svg(False), encoding='utf-8')
    vector = android_vector()
    (assets / 'logo-android.xml').write_text(vector, encoding='utf-8')
    (drawable / 'ic_brand.xml').write_text(vector, encoding='utf-8')
    (drawable / 'ic_brand_monochrome.xml').write_text(android_vector(True), encoding='utf-8')
    print('Generated SVGs and matching Android vector resources; no reference bitmap used.')
