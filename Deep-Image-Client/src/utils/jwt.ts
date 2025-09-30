export interface DecodedJwtPayload {
    loginId?: number | string;
    username?: string;
    email?: string;
    [key: string]: any;
}

export function decodeJwt(token: string): DecodedJwtPayload | null {
    try {
        const parts = token.split('.')
        if (parts.length !== 3) return null
        const payload = parts[1]
        const json = JSON.parse(decodeBase64Url(payload))
        return json as DecodedJwtPayload
    } catch {
        return null
    }
}

function decodeBase64Url(input: string): string {
    // base64url -> base64
    input = input.replace(/-/g, '+').replace(/_/g, '/');
    const pad = input.length % 4;
    if (pad) input = input + '='.repeat(4 - pad);
    const atobImpl = (typeof window !== 'undefined' && window.atob) ? window.atob : (typeof atob !== 'undefined' ? atob : null);
    if (!atobImpl) return '';
    try {
        return decodeURIComponent(escape(atobImpl(input)));
    } catch {
        return atobImpl(input);
    }
}


