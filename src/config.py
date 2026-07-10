"""Load config.yaml into a plain dict, resolved relative to project root."""
from __future__ import annotations

import pathlib
from typing import Any

import yaml

PROJECT_ROOT = pathlib.Path(__file__).resolve().parent.parent


def load_config(path: str | pathlib.Path | None = None) -> dict[str, Any]:
    cfg_path = pathlib.Path(path) if path else PROJECT_ROOT / "config.yaml"
    with open(cfg_path, "r", encoding="utf-8") as f:
        cfg = yaml.safe_load(f)

    log_dir = PROJECT_ROOT / cfg["runtime"]["log_dir"]
    log_dir.mkdir(parents=True, exist_ok=True)
    cfg["_resolved_log_dir"] = log_dir

    profile_dir = PROJECT_ROOT / cfg["game"]["profile_dir"]
    cfg["_resolved_profile_dir"] = profile_dir

    os_input_cfg = cfg["game"].get("os_input") or {}
    if os_input_cfg.get("user_data_dir"):
        cfg["_resolved_os_profile_dir"] = PROJECT_ROOT / os_input_cfg["user_data_dir"]

    templates_dir = PROJECT_ROOT / "templates"
    templates_dir.mkdir(parents=True, exist_ok=True)
    cfg["_resolved_templates_dir"] = templates_dir

    secrets_path = PROJECT_ROOT / "secrets.yaml"
    if secrets_path.exists():
        with open(secrets_path, "r", encoding="utf-8") as f:
            cfg["_secrets"] = yaml.safe_load(f) or {}
    else:
        cfg["_secrets"] = {}

    return cfg
