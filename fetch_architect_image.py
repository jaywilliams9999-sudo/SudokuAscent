import base64
import json
import urllib.request
import os

mermaid_code = """graph TD
    %% Slide-Optimized Compact Architecture
    classDef primary fill:#4f46e5,stroke:#312e81,color:#fff,stroke-width:3px,font-size:24px,padding:20px;
    classDef secondary fill:#059669,stroke:#064e3b,color:#fff,stroke-width:3px,font-size:24px,padding:20px;
    classDef tertiary fill:#9333ea,stroke:#4c1d95,color:#fff,stroke-width:3px,font-size:24px,padding:20px;

    Client((Web Browser)):::primary
    
    subgraph Core Architecture
        UI[React & Vite Frontend]:::primary
        Backend[Java Spring Boot Gateway]:::secondary
        AI[Python FastAPI Intelligence]:::tertiary
    end
    
    Client -->|Interacts| UI
    UI <-->|JSON over HTTP| Backend
    Backend <-->|Model Evaluation| AI
"""

payload = json.dumps({"code": mermaid_code, "mermaid": {"theme": "base", "themeVariables": {"fontSize": "22px"}, "backgroundColor": "transparent"}})
b64 = base64.b64encode(payload.encode('utf-8')).decode('utf-8')
url = "https://mermaid.ink/img/" + b64

output_path = os.path.join(os.getcwd(), 'system_architecture_logic.png')
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req) as response, open(output_path, 'wb') as out_file:
        data = response.read()
        out_file.write(data)
        print(f"Success! Diagram saved to {output_path}")
except Exception as e:
    print(f"Error: {e}")
