const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
// const BACKEND_URL = "http://localhost:8084";  // for local testing

/**
 * fetch profile picture file, and create an object url
 */
export async function fetchProfilePicture(username: string) {
    // fetch profile picture
    const params = new URLSearchParams({
        username: username,
    });

    // download file
    var res = await fetch(`${BACKEND_URL}/user-data/profile-picture?${params}`, { credentials: "include" });
    const s3objectUrl: String = await res.text();

    res = await fetch(`${s3objectUrl}`);
    const fileBlob = await res.blob();
    const objectUrl = URL.createObjectURL(fileBlob);
    return objectUrl;
}