import React, { useState } from "react";
import { obtenerVehiculo } from "../services/sriService";

export default function StepVehiculo({ onNext, persona, initial }) {
  const [placa, setPlaca] = useState(initial || "");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [vehiculo, setVehiculo] = useState(null);

  const buscar = async () => {
  const p = placa.trim().toUpperCase().replace(/[^A-Z0-9]/g, "");

  if (p.length < 6) {
    setError("Ingresa una placa válida");
    return;
  }

  setLoading(true);
  setError("");
  setVehiculo(null);

  try {
    const datos = await obtenerVehiculo(p);

    if (!datos) {
      setError("No se encontró el vehículo con esa placa");
      return;
    }

    setVehiculo(datos);

  } catch (e) {
    setError(e.message || "No se encontró información del vehículo");
  } finally {
    setLoading(false);
  }
};

  return (
    <div className="step-card" data-step="3">
      <div className="step-header">
        <span className="step-number">03</span>
        <h2>Matrícula del vehículo</h2>
        {persona && <p>Propietario: <strong>{persona.nombreCompleto || persona.razonSocial}</strong></p>}
      </div>

      <div className="field-group">
        <label htmlFor="placa">Número de placa</label>
        <div className="input-row">
          <input
            id="placa"
            type="text"
            maxLength={8}
            className={`field-input plate-input ${error ? "field-error" : ""}`}
            placeholder="ABC-1234"
            value={placa}
            onChange={(e) => { setPlaca(e.target.value.toUpperCase()); setError(""); setVehiculo(null); }}
          />
          <button className="btn-secondary" onClick={buscar} disabled={loading || placa.length < 6}>
            {loading ? <span className="spinner" /> : "Buscar"}
          </button>
        </div>
        {error && <span className="error-msg">{error}</span>}
      </div>

      {vehiculo && (
        <div className="result-card vehiculo">
          <div className="result-badge vehiculo">🚗 Vehículo encontrado</div>
          <div className="result-grid">
  {[
    ["Placa", vehiculo.numeroPlaca],
    ["Marca", vehiculo.descripcionMarca],
    ["Modelo", vehiculo.descripcionModelo],
    ["Año modelo", vehiculo.anioAuto],
    ["País", vehiculo.descripcionPais],
    ["Último año pagado", vehiculo.ultimoAnioPagado],
    ["CAMV / CPN", vehiculo.numeroCamvCpn],
    ["Exoneración", vehiculo.estadoExoneracion],
    ["Color 1", vehiculo.colorVehiculo1],
    ["Color 2", vehiculo.colorVehiculo2],
    ["Cilindraje", vehiculo.cilindraje ? `${vehiculo.cilindraje} cc` : null],
    ["Clase", vehiculo.nombreClase],
  ]
    .filter(([label, value]) => value !== null && value !== undefined && value !== "")
    .map(([label, value]) => (
      <div key={label} className="result-item">
        <span className="result-label">{label}</span>
        <span className="result-value">{value}</span>
      </div>
    ))}
</div>
          <button className="btn-primary" onClick={() => onNext({ placa: placa.replace("-",""), vehiculo })}>
            Continuar →
          </button>
        </div>
      )}
    </div>
  );
}
