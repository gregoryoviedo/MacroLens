# MacroLens — Product

This document captures *what MacroLens is for, who it serves, and what it deliberately will not do*. It is the reference when trade-offs come up. If a change conflicts with this document, either change the code or update this document on purpose — never silently.

## Vision

A pocket magnifier that is always there, asks for nothing, and tells no one. MacroLens exists so anyone can hold their phone up to small text, a screw head, a label, a skin blemish, or their own eye and see it bigger — without signing in, watching an ad, or sending a frame to the cloud.

## Target users

- **Low-vision readers** who need high-contrast views of printed text (menus, prescriptions, receipts, books). Served by the reading line, B&W and Inverted filters, and the front camera for self-checks.
- **Hobbyists and tinkerers** who need to inspect small mechanical or electronic parts. Served by high zoom, tap-to-focus, and the torch.
- **Everyday users** who occasionally need to read tiny print in poor light. Served by the always-on, no-account, instant-open nature of the app.
- **Self-examination** use cases: makeup, contact lenses, dental checks, skin spots. Served by the front camera and freeze.

## Use cases

1. **Reading small print.** Open the app, point the rear camera at the text, slide zoom, drag the reading line down as you read, freeze a difficult line and switch to B&W or Inverted to push contrast.
2. **Inspecting fine details.** Use pinch or the slider to get the highest usable zoom, tap the area you want sharp, use the torch when the room is dim, freeze to study the image without holding the phone still.
3. **Self-examination.** Switch to the front camera, freeze, and zoom in to study the frame in B&W or Inverted to remove skin-tone distraction.
4. **Quick check on a single line of text.** Freeze, switch to Inverted if the text is dark on a shiny surface, then resume.

## Non-goals

The following are intentionally **not** part of MacroLens. They have been considered and rejected because they would dilute the product or violate the privacy stance. New feature requests that fit any of these should be redirected.

- **No accounts, sign-in, or sync.** Every user is anonymous by construction.
- **No analytics, telemetry, or crash reporting.** There is no remote service to receive them.
- **No advertising, sponsorships, or promoted content.** The bottom sheet links to GitHub and Binance Pay for voluntary support; nothing more.
- **No image or video capture, gallery, or share.** The freeze frame stays in memory. Saving or sharing a frame would require storage permissions and a file path, both of which expand the attack surface.
- **No cloud processing or "AI enhancement".** Every pixel stays on the device.
- **No proprietary camera vendor extensions.** MacroLens uses CameraX only. Manufacturer super-resolution, night modes, and proprietary zooms are out of scope because they would couple the app to specific OEMs and break the universality promise.
- **No settings screen or "advanced mode".** Every option lives on the main screen as a single tap. The product grows by adding one more affordance, not by adding menus.
- **No foreground service or background camera.** The app only sees frames while it is in the foreground.

## Design principles

1. **One screen, one job.** The whole app is a camera preview with a few floating controls. Anything that does not fit on that screen probably does not belong in the app.
2. **Glass-on-black, no chrome.** Controls use translucent black surfaces with a thin white border. The preview is the product. The interface is the price of admission, not the destination.
3. **The system does the language work.** A user-facing string is one entry in `values/strings.xml` plus three translations. There is no runtime locale switcher.
4. **If a feature is risky, default it off.** The freeze filter starts at Normal, the reading line is hidden by default, the front camera is not selected at launch. The user opts in.
5. **Latency is a feature.** Focus lock, freeze, and filter application should feel instant. A feature that adds a perceptible delay before showing the result is the wrong feature.
6. **Honest about limits.** The README, the "More options" sheet, and the in-app license all say the same thing about what the app does and does not do. There is no marketing layer between the user and the code.

## When in doubt

A proposed change is on the right side of the line if it can be answered "yes" to all of:

- Does it stay on the same single screen, or get out of the way when the user does not need it?
- Does it work without a network connection?
- Does it work without a new runtime permission?
- Could a user explain it from a single button and its icon?
- Does it keep a frozen frame in memory, never on disk?

If any answer is "no", the change is not necessarily wrong — but the trade-off should be deliberate and the rationale should be added here.
