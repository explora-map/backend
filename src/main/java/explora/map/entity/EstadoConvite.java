package explora.map.entity;

/** Estado actual dun convite enviado. */
public enum EstadoConvite {
    /** O convite está á espera de resposta. */
    PENDENTE,
    /** O convite foi aceptado. */
    ACEPTADO,
    /** O convite foi rexeitado. */
    REXEITADO,
    /** O convite foi cancelado polo anfitrión. */
    CANCELADO,
    /** O convite caducou sen resposta. */
    EXPIRADO
}
