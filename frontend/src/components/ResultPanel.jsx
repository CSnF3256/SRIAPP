import React from "react";

export default function ResultPanel({ data, onReset }) {
  const { email, ruc, persona, vehiculo, placa, ant } = data;

  // Por seguridad, soporta varias formas posibles del objeto ANT
  const antDatos = ant?.ant || ant?.datos || ant || {};

  const puntos = antDatos?.puntosActuales;

  const puntosColor =
    puntos >= 25
      ? "var(--brand)"
      : puntos >= 15
      ? "var(--warn)"
      : "var(--danger)";

  const nombrePersona =
    persona?.razonSocial ||
    persona?.nombreCompleto ||
    "—";

  const estadoPersona =
    persona?.estadoContribuyenteRuc ||
    persona?.estadoContribuyente ||
    "—";

  const placaVehiculo =
    vehiculo?.numeroPlaca ||
    placa ||
    "—";

  const marcaModelo =
    vehiculo?.descripcionMarca || vehiculo?.descripcionModelo
      ? `${vehiculo?.descripcionMarca || ""} ${vehiculo?.descripcionModelo || ""}`.trim()
      : "—";

  const anioVehiculo =
    vehiculo?.anioAuto ||
    vehiculo?.anioFabricacion ||
    "—";

  const paisVehiculo =
    vehiculo?.descripcionPais ||
    "—";

  const ultimoAnioPagado =
    vehiculo?.ultimoAnioPagado ||
    "—";

  return (
    <div className="step-card">
      <div className="step-header">
        <span className="step-number">✓ Completado</span>
        <h2>Resumen de verificación</h2>
        <p>{email || "—"}</p>
      </div>

      <section style={{ marginBottom: 20 }}>
        <h3
          style={{
            fontSize: 13,
            fontWeight: 600,
            marginBottom: 10,
            textTransform: "uppercase",
            letterSpacing: "0.07em",
            color: "var(--muted)",
          }}
        >
          Datos SRI
        </h3>

        <div className="result-grid">
          <div className="result-item">
            <span className="result-label">Nombre</span>
            <span className="result-value">{nombrePersona}</span>
          </div>

          <div className="result-item">
            <span className="result-label">RUC</span>
            <span className="result-value">{persona?.numeroRuc || ruc || "—"}</span>
          </div>

          <div className="result-item">
            <span className="result-label">Estado</span>
            <span className="result-value">{estadoPersona}</span>
          </div>

          <div className="result-item">
            <span className="result-label">Tipo</span>
            <span className="result-value">{persona?.tipoContribuyente || "—"}</span>
          </div>

          <div className="result-item">
            <span className="result-label">Régimen</span>
            <span className="result-value">{persona?.regimen || "—"}</span>
          </div>
        </div>
      </section>

      <section style={{ marginBottom: 20 }}>
        <h3
          style={{
            fontSize: 13,
            fontWeight: 600,
            marginBottom: 10,
            textTransform: "uppercase",
            letterSpacing: "0.07em",
            color: "var(--muted)",
          }}
        >
          Vehículo
        </h3>

        <div className="result-grid">
          <div className="result-item">
            <span className="result-label">Placa</span>
            <span className="result-value">{placaVehiculo}</span>
          </div>

          <div className="result-item">
            <span className="result-label">Marca / Modelo</span>
            <span className="result-value">{marcaModelo}</span>
          </div>

          <div className="result-item">
            <span className="result-label">Año modelo</span>
            <span className="result-value">{anioVehiculo}</span>
          </div>

          <div className="result-item">
            <span className="result-label">País</span>
            <span className="result-value">{paisVehiculo}</span>
          </div>

          <div className="result-item">
            <span className="result-label">Último año pagado</span>
            <span className="result-value">{ultimoAnioPagado}</span>
          </div>

          <div className="result-item">
            <span className="result-label">CAMV / CPN</span>
            <span className="result-value">{vehiculo?.numeroCamvCpn || "—"}</span>
          </div>
        </div>
      </section>

      <section>
        <h3
          style={{
            fontSize: 13,
            fontWeight: 600,
            marginBottom: 10,
            textTransform: "uppercase",
            letterSpacing: "0.07em",
            color: "var(--muted)",
          }}
        >
          Licencia ANT
        </h3>

        <div style={{ display: "flex", alignItems: "baseline", gap: 8 }}>
          <span
            style={{
              fontFamily: "'Syne', sans-serif",
              fontSize: 40,
              fontWeight: 700,
              color: puntosColor,
            }}
          >
            {puntos ?? "—"}
          </span>
          <span style={{ color: "var(--muted)" }}>/ 30 puntos</span>
        </div>

        <div className="result-grid" style={{ marginTop: 14 }}>
          <div className="result-item">
            <span className="result-label">Puntos descontados</span>
            <span className="result-value">{antDatos?.puntosDescontados ?? "—"}</span>
          </div>

          <div className="result-item">
            <span className="result-label">Total citaciones</span>
            <span className="result-value">{antDatos?.totalCitaciones ?? "—"}</span>
          </div>

          <div className="result-item">
            <span className="result-label">Actualización</span>
            <span className="result-value">{antDatos?.cacheAge || "—"}</span>
          </div>
        </div>
      </section>

      <button className="btn-primary" onClick={onReset} style={{ marginTop: 28 }}>
        Nueva consulta
      </button>
    </div>
  );
}