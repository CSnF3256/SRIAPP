import React, { useState, useEffect } from "react";
import { obtenerPuntosANT } from "../services/antService";

export default function StepANT({ onNext, cedula, placa }) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [ant, setAnt] = useState(null);
  const [fromCache, setFromCache] = useState(false);

  useEffect(() => {
  if (cedula && placa) {
    consultar();
  } else {
    setLoading(false);
    setError("No se recibieron la cédula y la placa para consultar la ANT");
  }
}, [cedula, placa]);

  const consultar = async () => {
  setLoading(true);
  setError("");
  setAnt(null);

  console.log("Datos recibidos en StepANT:", { cedula, placa });

  try {
    const result = await obtenerPuntosANT(cedula, placa);
    setAnt(result.datos);
    setFromCache(result.fromCache);
  } catch (e) {
    setError(e.message);
  } finally {
    setLoading(false);
  }
};

  const puntosColor = (p) => {
    if (p >= 25) return "points-good";
    if (p >= 15) return "points-warn";
    return "points-danger";
  };

  return (
    <div className="step-card" data-step="4">
      <div className="step-header">
        <span className="step-number">04</span>
        <h2>Puntos en licencia</h2>
        <p>Consultando la Agencia Nacional de Tránsito</p>
      </div>

      {loading && (
        <div className="loading-block">
          <span className="spinner large" />
          <p>Consultando ANT… esto puede tomar un momento</p>
        </div>
      )}

      {error && (
        <div className="error-block">
          <p>⚠ {error}</p>
          <p className="error-sub">La ANT tiene baja disponibilidad. Intenta de nuevo.</p>
          <button className="btn-secondary" onClick={consultar}>Reintentar</button>
        </div>
      )}

      {ant && (
        <div className="result-card ant">
          {fromCache && (
            <div className="cache-badge">⚡ Datos desde caché · actualizado {ant.cacheAge || "hoy"}</div>
          )}
          <div className="points-display">
            <span className={`points-number ${puntosColor(ant.puntosActuales)}`}>
              {ant.puntosActuales}
            </span>
            <span className="points-of">/ 30 puntos</span>
          </div>
          <div className="points-bar-wrap">
            <div className="points-bar-bg">
              <div
                className={`points-bar-fill ${puntosColor(ant.puntosActuales)}`}
                style={{ width: `${(ant.puntosActuales / 30) * 100}%` }}
              />
            </div>
          </div>

          {ant.citaciones && ant.citaciones.length > 0 && (
            <div className="citaciones">
              <h3>Citaciones registradas ({ant.citaciones.length})</h3>
              <table className="cit-table">
                <thead>
                  <tr><th>Fecha</th><th>Infracción</th><th>Puntos</th><th>Estado</th></tr>
                </thead>
                <tbody>
                  {ant.citaciones.map((c, i) => (
                    <tr key={i}>
                      <td>{c.fecha}</td>
                      <td>{c.descripcion}</td>
                      <td>{c.puntos}</td>
                      <td><span className={`estado-badge ${c.estado?.toLowerCase()}`}>{c.estado}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <button className="btn-primary" onClick={() => onNext({ ant })}>
            Ver resumen completo →
          </button>
        </div>
      )}
    </div>
  );
}
