import React, { useState } from "react";
import { verificarContribuyente, obtenerPersona } from "../services/sriService";

export default function StepRUC({ onNext, email, initial }) {
  const [ruc, setRuc] = useState(initial || "");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [persona, setPersona] = useState(null);

  const verificar = async () => {
    const r = ruc.trim();

    if (r.length !== 13) {
      setError("El RUC debe tener 13 dígitos");
      return;
    }

    if (!r.endsWith("001")) {
      setError("Para persona natural el RUC termina en 001");
      return;
    }

    setLoading(true);
    setError("");
    setPersona(null);

    try {
      const esContribuyente = await verificarContribuyente(r);

      if (!esContribuyente) {
        setError("El número ingresado no corresponde a un contribuyente del SRI");
        return;
      }

      const datos = await obtenerPersona(r);

      if (!datos || !datos.tipoContribuyente?.toUpperCase().includes("NATURAL")) {
        setError("Este RUC no corresponde a una persona natural");
        return;
      }

      setPersona(datos);
    } catch (e) {
      setError("Error al consultar el SRI: " + e.message);
    } finally {
      setLoading(false);
    }
  };

  const confirmar = () => {
    onNext({ ruc, persona, contribuyente: true });
  };

  return (
    <div className="step-card" data-step="2">
      <div className="step-header">
        <span className="step-number">02</span>
        <h2>Verificación SRI</h2>
        <p>Ingresa el RUC de la persona natural a verificar</p>
      </div>

      <div className="field-group">
        <label htmlFor="ruc">Número de RUC</label>
        <div className="input-row">
          <input
            id="ruc"
            type="text"
            maxLength={13}
            className={`field-input ${error ? "field-error" : ""}`}
            placeholder="1712345678001"
            value={ruc}
            onChange={(e) => {
              setRuc(e.target.value.replace(/\D/g, ""));
              setError("");
              setPersona(null);
            }}
          />
          <button
            className="btn-secondary"
            onClick={verificar}
            disabled={loading || ruc.length !== 13}
          >
            {loading ? "Verificando..." : "Verificar"}
          </button>
        </div>
        {error && <span className="error-msg">{error}</span>}
      </div>

      {persona && (
        <div className="result-card sri">
          <div className="result-badge sri">
            ✓ Contribuyente verificado — Persona Natural
          </div>

          <div className="result-grid">
            <div className="result-item">
              <span className="result-label">Nombre</span>
              <span className="result-value">
                {persona.razonSocial || persona.nombreCompleto || "—"}
              </span>
            </div>

            <div className="result-item">
              <span className="result-label">Estado</span>
              <span className="result-value">
                {persona.estadoContribuyenteRuc || "—"}
              </span>
            </div>

            <div className="result-item">
              <span className="result-label">Actividad</span>
              <span className="result-value">
                {persona.actividadEconomicaPrincipal || "—"}
              </span>
            </div>

            <div className="result-item">
              <span className="result-label">Correo</span>
              <span className="result-value">
                {email || "—"}
              </span>
            </div>

            <div className="result-item">
              <span className="result-label">RUC</span>
              <span className="result-value">
                {persona.numeroRuc || ruc || "—"}
              </span>
            </div>

            <div className="result-item">
              <span className="result-label">Tipo</span>
              <span className="result-value">
                {persona.tipoContribuyente || "—"}
              </span>
            </div>

            <div className="result-item">
              <span className="result-label">Régimen</span>
              <span className="result-value">
                {persona.regimen || "—"}
              </span>
            </div>

            <div className="result-item">
              <span className="result-label">Obligado a llevar contabilidad</span>
              <span className="result-value">
                {persona.obligadoLlevarContabilidad || "—"}
              </span>
            </div>
          </div>

          <button className="btn-primary" onClick={confirmar}>
            Continuar →
          </button>
        </div>
      )}
    </div>
  );
}