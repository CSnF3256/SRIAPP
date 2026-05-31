const BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080/api";

export async function obtenerPuntosANT(cedula, placa) {
  const cedulaNorm = String(cedula || "").replace(/\D/g, "");
  const placaNorm = String(placa || "").toUpperCase().replace(/[^A-Z0-9]/g, "");

  const url = `${BASE}/ant/licencia?cedula=${encodeURIComponent(cedulaNorm)}&placa=${encodeURIComponent(placaNorm)}`;

  console.log("Consultando ANT:", url);

  const res = await fetch(url);

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.mensaje || err.error || "Error consultando la ANT");
  }

  return await res.json();
}