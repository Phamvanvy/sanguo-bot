"""Pluggable text similarity for RAG retrieval over quest memory.

Two implementations, selected by memory.embedder in config.yaml:
  - "local" (default): TF-IDF + cosine similarity, scikit-learn, no network
    call, no model weights beyond what's already in the venv. Good enough
    here because the corpus is quest titles -- a few hundred short strings,
    not free-form prose -- where lexical overlap already captures most of
    the signal.
  - "cloud": OpenAI embeddings API. Requires cloud.openai_api_key in
    secrets.yaml. Kept as a second implementation specifically so the same
    retrieve.py caller can compare a real embedding model against the local
    TF-IDF baseline without touching retrieval logic.

Both expose the same shape: similarities(query, corpus) -> np.ndarray of
cosine similarity scores, one per corpus item, aligned by index.
"""
from __future__ import annotations

from typing import Protocol

import numpy as np


class Embedder(Protocol):
    def similarities(self, query: str, corpus: list[str]) -> np.ndarray: ...


class LocalTfidfEmbedder:
    """Refits a fresh TF-IDF vectorizer on [query] + corpus every call.
    Deliberately stateless/non-persisted: refitting is O(corpus size) which
    is trivial at this scale, and it sidesteps vocabulary drift you'd get
    from a vectorizer fit once and reused as new quest titles appear."""

    def similarities(self, query: str, corpus: list[str]) -> np.ndarray:
        if not corpus:
            return np.zeros(0)
        from sklearn.feature_extraction.text import TfidfVectorizer
        from sklearn.metrics.pairwise import cosine_similarity

        vectorizer = TfidfVectorizer(analyzer="char_wb", ngram_range=(2, 4))
        matrix = vectorizer.fit_transform([query] + corpus)
        sims = cosine_similarity(matrix[0:1], matrix[1:]).ravel()
        return sims


class CloudOpenAIEmbedder:
    def __init__(self, api_key: str, model: str = "text-embedding-3-small"):
        from openai import OpenAI

        self._client = OpenAI(api_key=api_key)
        self._model = model

    def similarities(self, query: str, corpus: list[str]) -> np.ndarray:
        if not corpus:
            return np.zeros(0)
        from sklearn.metrics.pairwise import cosine_similarity

        resp = self._client.embeddings.create(model=self._model, input=[query] + corpus)
        vectors = np.array([d.embedding for d in resp.data])
        sims = cosine_similarity(vectors[0:1], vectors[1:]).ravel()
        return sims


def get_embedder(cfg: dict) -> Embedder:
    kind = cfg.get("memory", {}).get("embedder", "local")
    if kind == "cloud":
        api_key = (cfg.get("_secrets", {}).get("cloud") or {}).get("openai_api_key")
        if not api_key:
            raise RuntimeError(
                "memory.embedder=cloud requires cloud.openai_api_key in secrets.yaml"
            )
        model = cfg.get("memory", {}).get("cloud_embedding_model", "text-embedding-3-small")
        return CloudOpenAIEmbedder(api_key=api_key, model=model)
    return LocalTfidfEmbedder()
