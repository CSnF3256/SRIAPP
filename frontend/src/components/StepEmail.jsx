import React, { useState } from "react";

export default function StepEmail({ onNext, initial }) {
  const [email, setEmail] = useState(initial || "");
  const [error, setError] = useState("");

  const validate = () => {
    if (!email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
      setError("Ingresa un correo electrónico válido");
      return;
    }
    onNext({ email });
  };

  return (
    <div className="step-card" data-step="1">
      <div className="step-header">
        <span className="step-number">01</span>
        <h2>Datos de contacto</h2>
        <p>Ingresa tu correo electrónico para iniciar la verificación</p>
      </div>
      <div className="field-group">
        <label htmlFor="email">Correo electrónico</label>
        <input
          id="email"
          type="email"
          className={`field-input ${error ? "field-error" : ""}`}
          placeholder="ejemplo@correo.com"
          value={email}
          onChange={(e) => { setEmail(e.target.value); setError(""); }}
          onKeyDown={(e) => e.key === "Enter" && validate()}
        />
        {error && <span className="error-msg">{error}</span>}
      </div>
      <button className="btn-primary" onClick={validate}>
        Continuar →
      </button>
    </div>
  );
}
