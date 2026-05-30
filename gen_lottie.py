import json
import os

base_json = {
  "v": "5.5.2",
  "fr": 60,
  "ip": 0,
  "op": 60,
  "w": 512,
  "h": 512,
  "nm": "Comp 1",
  "ddd": 0,
  "assets": [],
  "layers": [
    {
      "ddd": 0,
      "ind": 1,
      "ty": 4,
      "nm": "Shape Layer 1",
      "sr": 1,
      "ks": {
        "o": {"a": 0, "k": 100, "ix": 11},
        "r": {"a": 0, "k": 0, "ix": 10},
        "p": {"a": 0, "k": [256, 256, 0], "ix": 2},
        "a": {"a": 0, "k": [0, 0, 0], "ix": 1},
        "s": {"a": 0, "k": [100, 100, 100], "ix": 6}
      },
      "ao": 0,
      "shapes": [
        {
          "ty": "gr",
          "it": [
            {
              "d": 1,
              "ty": "el",
              "s": {"a": 1, "k": [{"i": {"x": 0.833, "y": 0.833}, "o": {"x": 0.167, "y": 0.167}, "t": 0, "s": [200, 200]}, {"t": 30, "s": [300, 300]}, {"t": 60, "s": [200, 200]}], "ix": 2},
              "p": {"a": 0, "k": [0, 0], "ix": 3},
              "nm": "Ellipse Path 1",
              "mn": "ADBE Vector Shape - Ellipse",
              "hd": False
            },
            {
              "ty": "fl",
              "c": {"a": 0, "k": [0.2, 0.8, 0.2, 1], "ix": 4},
              "o": {"a": 0, "k": 100, "ix": 5},
              "r": 1,
              "bm": 0,
              "nm": "Fill 1",
              "mn": "ADBE Vector Graphic - Fill",
              "hd": False
            },
            {
              "ty": "tr",
              "p": {"a": 0, "k": [0, 0], "ix": 2},
              "a": {"a": 0, "k": [0, 0], "ix": 1},
              "s": {"a": 0, "k": [100, 100], "ix": 3},
              "r": {"a": 0, "k": 0, "ix": 6},
              "o": {"a": 0, "k": 100, "ix": 7},
              "sk": {"a": 0, "k": 0, "ix": 4},
              "sa": {"a": 0, "k": 0, "ix": 5},
              "nm": "Transform"
            }
          ],
          "nm": "Ellipse 1",
          "np": 3,
          "cix": 2,
          "bm": 0,
          "ix": 1,
          "mn": "ADBE Vector Group",
          "hd": False
        }
      ],
      "ip": 0,
      "op": 60,
      "st": 0,
      "bm": 0
    }
  ]
}

colors = {
    "lottie_balanced": [0.2, 0.6, 1.0, 1.0],  # Blue
    "lottie_deep": [0.2, 0.8, 0.2, 1.0],      # Green
    "lottie_hibernation": [0.6, 0.8, 0.9, 1.0], # Ice Blue
    "lottie_burst": [1.0, 0.2, 0.2, 1.0],     # Red
    "lottie_nightwatch": [0.3, 0.1, 0.6, 1.0] # Purple
}

for name, color in colors.items():
    data = json.loads(json.dumps(base_json))
    data["layers"][0]["shapes"][0]["it"][1]["c"]["k"] = color
    with open(f"D:/Projects/zoron/src/WhyredController/app/src/main/res/raw/{name}.json", "w") as f:
        json.dump(data, f)
