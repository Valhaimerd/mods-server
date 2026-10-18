"""Alias for the canonical CrossArms/Expanded integration build."""
from pathlib import Path
import runpy
runpy.run_path(str(Path(__file__).resolve().parents[1]/"bows-balance/build.py"),run_name="__main__")
