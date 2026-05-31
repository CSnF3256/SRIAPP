const BASE = "/api";

export async function verificarContribuyente(ruc) {
  const res = await fetch(`${BASE}/sri/contribuyente/verificar?ruc=${ruc}`);
  if (!res.ok) throw new Error("Error consultando SRI");
  const data = await res.json();
  return data.esContribuyente === true;
}

export async function obtenerPersona(ruc) {
  const res = await fetch(`${BASE}/sri/persona?ruc=${ruc}`);
  if (!res.ok) throw new Error("Error obteniendo datos de persona");
  return res.json();
}

export async function obtenerVehiculo(placa) {
  const res = await fetch(`${BASE}/sri/vehiculo?placa=${placa}`);
  if (!res.ok) throw new Error("Error obteniendo datos del vehículo");
  return res.json();
}