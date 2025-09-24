/*eslint-disable block-scoped-var, id-length, no-control-regex, no-magic-numbers, no-prototype-builtins, no-redeclare, no-shadow, no-var, sort-vars*/
import * as $protobuf from "protobufjs/minimal";

// Common aliases
const $Reader = $protobuf.Reader, $Writer = $protobuf.Writer, $util = $protobuf.util;

// Exported root namespace
const $root = $protobuf.roots["default"] || ($protobuf.roots["default"] = {});

export const edu = $root.edu = (() => {

    /**
     * Namespace edu.
     * @exports edu
     * @namespace
     */
    const edu = {};

    edu.cust = (function() {

        /**
         * Namespace cust.
         * @memberof edu
         * @namespace
         */
        const cust = {};

        cust.secad = (function() {

            /**
             * Namespace secad.
             * @memberof edu.cust
             * @namespace
             */
            const secad = {};

            secad.chat = (function() {

                /**
                 * Namespace chat.
                 * @memberof edu.cust.secad
                 * @namespace
                 */
                const chat = {};

                chat.proto = (function() {

                    /**
                     * Namespace proto.
                     * @memberof edu.cust.secad.chat
                     * @namespace
                     */
                    const proto = {};

                    proto.ChatMessageProtocolForm = (function() {

                        /**
                         * Properties of a ChatMessageProtocolForm.
                         * @memberof edu.cust.secad.chat.proto
                         * @interface IChatMessageProtocolForm
                         * @property {string|null} [lastUid] ChatMessageProtocolForm lastUid
                         * @property {string|null} [sendTime] ChatMessageProtocolForm sendTime
                         * @property {string|null} [pkgId] ChatMessageProtocolForm pkgId
                         * @property {string|null} [senderId] ChatMessageProtocolForm senderId
                         * @property {string|null} [rcvId] ChatMessageProtocolForm rcvId
                         * @property {string|null} [unionId] ChatMessageProtocolForm unionId
                         * @property {number|null} [len] ChatMessageProtocolForm len
                         * @property {number|null} [msgType] ChatMessageProtocolForm msgType
                         * @property {Uint8Array|null} [messageContent] ChatMessageProtocolForm messageContent
                         */

                        /**
                         * Constructs a new ChatMessageProtocolForm.
                         * @memberof edu.cust.secad.chat.proto
                         * @classdesc Represents a ChatMessageProtocolForm.
                         * @implements IChatMessageProtocolForm
                         * @constructor
                         * @param {edu.cust.secad.chat.proto.IChatMessageProtocolForm=} [properties] Properties to set
                         */
                        function ChatMessageProtocolForm(properties) {
                            if (properties)
                                for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                    if (properties[keys[i]] != null)
                                        this[keys[i]] = properties[keys[i]];
                        }

                        /**
                         * ChatMessageProtocolForm lastUid.
                         * @member {string} lastUid
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @instance
                         */
                        ChatMessageProtocolForm.prototype.lastUid = "";

                        /**
                         * ChatMessageProtocolForm sendTime.
                         * @member {string} sendTime
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @instance
                         */
                        ChatMessageProtocolForm.prototype.sendTime = "";

                        /**
                         * ChatMessageProtocolForm pkgId.
                         * @member {string} pkgId
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @instance
                         */
                        ChatMessageProtocolForm.prototype.pkgId = "";

                        /**
                         * ChatMessageProtocolForm senderId.
                         * @member {string} senderId
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @instance
                         */
                        ChatMessageProtocolForm.prototype.senderId = "";

                        /**
                         * ChatMessageProtocolForm rcvId.
                         * @member {string} rcvId
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @instance
                         */
                        ChatMessageProtocolForm.prototype.rcvId = "";

                        /**
                         * ChatMessageProtocolForm unionId.
                         * @member {string} unionId
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @instance
                         */
                        ChatMessageProtocolForm.prototype.unionId = "";

                        /**
                         * ChatMessageProtocolForm len.
                         * @member {number} len
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @instance
                         */
                        ChatMessageProtocolForm.prototype.len = 0;

                        /**
                         * ChatMessageProtocolForm msgType.
                         * @member {number} msgType
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @instance
                         */
                        ChatMessageProtocolForm.prototype.msgType = 0;

                        /**
                         * ChatMessageProtocolForm messageContent.
                         * @member {Uint8Array} messageContent
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @instance
                         */
                        ChatMessageProtocolForm.prototype.messageContent = $util.newBuffer([]);

                        /**
                         * Creates a new ChatMessageProtocolForm instance using the specified properties.
                         * @function create
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @static
                         * @param {edu.cust.secad.chat.proto.IChatMessageProtocolForm=} [properties] Properties to set
                         * @returns {edu.cust.secad.chat.proto.ChatMessageProtocolForm} ChatMessageProtocolForm instance
                         */
                        ChatMessageProtocolForm.create = function create(properties) {
                            return new ChatMessageProtocolForm(properties);
                        };

                        /**
                         * Encodes the specified ChatMessageProtocolForm message. Does not implicitly {@link edu.cust.secad.chat.proto.ChatMessageProtocolForm.verify|verify} messages.
                         * @function encode
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @static
                         * @param {edu.cust.secad.chat.proto.IChatMessageProtocolForm} message ChatMessageProtocolForm message or plain object to encode
                         * @param {$protobuf.Writer} [writer] Writer to encode to
                         * @returns {$protobuf.Writer} Writer
                         */
                        ChatMessageProtocolForm.encode = function encode(message, writer) {
                            if (!writer)
                                writer = $Writer.create();
                            if (message.lastUid != null && Object.hasOwnProperty.call(message, "lastUid"))
                                writer.uint32(/* id 1, wireType 2 =*/10).string(message.lastUid);
                            if (message.sendTime != null && Object.hasOwnProperty.call(message, "sendTime"))
                                writer.uint32(/* id 2, wireType 2 =*/18).string(message.sendTime);
                            if (message.pkgId != null && Object.hasOwnProperty.call(message, "pkgId"))
                                writer.uint32(/* id 3, wireType 2 =*/26).string(message.pkgId);
                            if (message.senderId != null && Object.hasOwnProperty.call(message, "senderId"))
                                writer.uint32(/* id 4, wireType 2 =*/34).string(message.senderId);
                            if (message.rcvId != null && Object.hasOwnProperty.call(message, "rcvId"))
                                writer.uint32(/* id 5, wireType 2 =*/42).string(message.rcvId);
                            if (message.unionId != null && Object.hasOwnProperty.call(message, "unionId"))
                                writer.uint32(/* id 6, wireType 2 =*/50).string(message.unionId);
                            if (message.len != null && Object.hasOwnProperty.call(message, "len"))
                                writer.uint32(/* id 7, wireType 0 =*/56).int32(message.len);
                            if (message.msgType != null && Object.hasOwnProperty.call(message, "msgType"))
                                writer.uint32(/* id 8, wireType 0 =*/64).int32(message.msgType);
                            if (message.messageContent != null && Object.hasOwnProperty.call(message, "messageContent"))
                                writer.uint32(/* id 9, wireType 2 =*/74).bytes(message.messageContent);
                            return writer;
                        };

                        /**
                         * Encodes the specified ChatMessageProtocolForm message, length delimited. Does not implicitly {@link edu.cust.secad.chat.proto.ChatMessageProtocolForm.verify|verify} messages.
                         * @function encodeDelimited
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @static
                         * @param {edu.cust.secad.chat.proto.IChatMessageProtocolForm} message ChatMessageProtocolForm message or plain object to encode
                         * @param {$protobuf.Writer} [writer] Writer to encode to
                         * @returns {$protobuf.Writer} Writer
                         */
                        ChatMessageProtocolForm.encodeDelimited = function encodeDelimited(message, writer) {
                            return this.encode(message, writer).ldelim();
                        };

                        /**
                         * Decodes a ChatMessageProtocolForm message from the specified reader or buffer.
                         * @function decode
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @static
                         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                         * @param {number} [length] Message length if known beforehand
                         * @returns {edu.cust.secad.chat.proto.ChatMessageProtocolForm} ChatMessageProtocolForm
                         * @throws {Error} If the payload is not a reader or valid buffer
                         * @throws {$protobuf.util.ProtocolError} If required fields are missing
                         */
                        ChatMessageProtocolForm.decode = function decode(reader, length, error) {
                            if (!(reader instanceof $Reader))
                                reader = $Reader.create(reader);
                            let end = length === undefined ? reader.len : reader.pos + length, message = new $root.edu.cust.secad.chat.proto.ChatMessageProtocolForm();
                            while (reader.pos < end) {
                                let tag = reader.uint32();
                                if (tag === error)
                                    break;
                                switch (tag >>> 3) {
                                case 1: {
                                        message.lastUid = reader.string();
                                        break;
                                    }
                                case 2: {
                                        message.sendTime = reader.string();
                                        break;
                                    }
                                case 3: {
                                        message.pkgId = reader.string();
                                        break;
                                    }
                                case 4: {
                                        message.senderId = reader.string();
                                        break;
                                    }
                                case 5: {
                                        message.rcvId = reader.string();
                                        break;
                                    }
                                case 6: {
                                        message.unionId = reader.string();
                                        break;
                                    }
                                case 7: {
                                        message.len = reader.int32();
                                        break;
                                    }
                                case 8: {
                                        message.msgType = reader.int32();
                                        break;
                                    }
                                case 9: {
                                        message.messageContent = reader.bytes();
                                        break;
                                    }
                                default:
                                    reader.skipType(tag & 7);
                                    break;
                                }
                            }
                            return message;
                        };

                        /**
                         * Decodes a ChatMessageProtocolForm message from the specified reader or buffer, length delimited.
                         * @function decodeDelimited
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @static
                         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                         * @returns {edu.cust.secad.chat.proto.ChatMessageProtocolForm} ChatMessageProtocolForm
                         * @throws {Error} If the payload is not a reader or valid buffer
                         * @throws {$protobuf.util.ProtocolError} If required fields are missing
                         */
                        ChatMessageProtocolForm.decodeDelimited = function decodeDelimited(reader) {
                            if (!(reader instanceof $Reader))
                                reader = new $Reader(reader);
                            return this.decode(reader, reader.uint32());
                        };

                        /**
                         * Verifies a ChatMessageProtocolForm message.
                         * @function verify
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @static
                         * @param {Object.<string,*>} message Plain object to verify
                         * @returns {string|null} `null` if valid, otherwise the reason why it is not
                         */
                        ChatMessageProtocolForm.verify = function verify(message) {
                            if (typeof message !== "object" || message === null)
                                return "object expected";
                            if (message.lastUid != null && message.hasOwnProperty("lastUid"))
                                if (!$util.isString(message.lastUid))
                                    return "lastUid: string expected";
                            if (message.sendTime != null && message.hasOwnProperty("sendTime"))
                                if (!$util.isString(message.sendTime))
                                    return "sendTime: string expected";
                            if (message.pkgId != null && message.hasOwnProperty("pkgId"))
                                if (!$util.isString(message.pkgId))
                                    return "pkgId: string expected";
                            if (message.senderId != null && message.hasOwnProperty("senderId"))
                                if (!$util.isString(message.senderId))
                                    return "senderId: string expected";
                            if (message.rcvId != null && message.hasOwnProperty("rcvId"))
                                if (!$util.isString(message.rcvId))
                                    return "rcvId: string expected";
                            if (message.unionId != null && message.hasOwnProperty("unionId"))
                                if (!$util.isString(message.unionId))
                                    return "unionId: string expected";
                            if (message.len != null && message.hasOwnProperty("len"))
                                if (!$util.isInteger(message.len))
                                    return "len: integer expected";
                            if (message.msgType != null && message.hasOwnProperty("msgType"))
                                if (!$util.isInteger(message.msgType))
                                    return "msgType: integer expected";
                            if (message.messageContent != null && message.hasOwnProperty("messageContent"))
                                if (!(message.messageContent && typeof message.messageContent.length === "number" || $util.isString(message.messageContent)))
                                    return "messageContent: buffer expected";
                            return null;
                        };

                        /**
                         * Creates a ChatMessageProtocolForm message from a plain object. Also converts values to their respective internal types.
                         * @function fromObject
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @static
                         * @param {Object.<string,*>} object Plain object
                         * @returns {edu.cust.secad.chat.proto.ChatMessageProtocolForm} ChatMessageProtocolForm
                         */
                        ChatMessageProtocolForm.fromObject = function fromObject(object) {
                            if (object instanceof $root.edu.cust.secad.chat.proto.ChatMessageProtocolForm)
                                return object;
                            let message = new $root.edu.cust.secad.chat.proto.ChatMessageProtocolForm();
                            if (object.lastUid != null)
                                message.lastUid = String(object.lastUid);
                            if (object.sendTime != null)
                                message.sendTime = String(object.sendTime);
                            if (object.pkgId != null)
                                message.pkgId = String(object.pkgId);
                            if (object.senderId != null)
                                message.senderId = String(object.senderId);
                            if (object.rcvId != null)
                                message.rcvId = String(object.rcvId);
                            if (object.unionId != null)
                                message.unionId = String(object.unionId);
                            if (object.len != null)
                                message.len = object.len | 0;
                            if (object.msgType != null)
                                message.msgType = object.msgType | 0;
                            if (object.messageContent != null)
                                if (typeof object.messageContent === "string")
                                    $util.base64.decode(object.messageContent, message.messageContent = $util.newBuffer($util.base64.length(object.messageContent)), 0);
                                else if (object.messageContent.length >= 0)
                                    message.messageContent = object.messageContent;
                            return message;
                        };

                        /**
                         * Creates a plain object from a ChatMessageProtocolForm message. Also converts values to other types if specified.
                         * @function toObject
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @static
                         * @param {edu.cust.secad.chat.proto.ChatMessageProtocolForm} message ChatMessageProtocolForm
                         * @param {$protobuf.IConversionOptions} [options] Conversion options
                         * @returns {Object.<string,*>} Plain object
                         */
                        ChatMessageProtocolForm.toObject = function toObject(message, options) {
                            if (!options)
                                options = {};
                            let object = {};
                            if (options.defaults) {
                                object.lastUid = "";
                                object.sendTime = "";
                                object.pkgId = "";
                                object.senderId = "";
                                object.rcvId = "";
                                object.unionId = "";
                                object.len = 0;
                                object.msgType = 0;
                                if (options.bytes === String)
                                    object.messageContent = "";
                                else {
                                    object.messageContent = [];
                                    if (options.bytes !== Array)
                                        object.messageContent = $util.newBuffer(object.messageContent);
                                }
                            }
                            if (message.lastUid != null && message.hasOwnProperty("lastUid"))
                                object.lastUid = message.lastUid;
                            if (message.sendTime != null && message.hasOwnProperty("sendTime"))
                                object.sendTime = message.sendTime;
                            if (message.pkgId != null && message.hasOwnProperty("pkgId"))
                                object.pkgId = message.pkgId;
                            if (message.senderId != null && message.hasOwnProperty("senderId"))
                                object.senderId = message.senderId;
                            if (message.rcvId != null && message.hasOwnProperty("rcvId"))
                                object.rcvId = message.rcvId;
                            if (message.unionId != null && message.hasOwnProperty("unionId"))
                                object.unionId = message.unionId;
                            if (message.len != null && message.hasOwnProperty("len"))
                                object.len = message.len;
                            if (message.msgType != null && message.hasOwnProperty("msgType"))
                                object.msgType = message.msgType;
                            if (message.messageContent != null && message.hasOwnProperty("messageContent"))
                                object.messageContent = options.bytes === String ? $util.base64.encode(message.messageContent, 0, message.messageContent.length) : options.bytes === Array ? Array.prototype.slice.call(message.messageContent) : message.messageContent;
                            return object;
                        };

                        /**
                         * Converts this ChatMessageProtocolForm to JSON.
                         * @function toJSON
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @instance
                         * @returns {Object.<string,*>} JSON object
                         */
                        ChatMessageProtocolForm.prototype.toJSON = function toJSON() {
                            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                        };

                        /**
                         * Gets the default type url for ChatMessageProtocolForm
                         * @function getTypeUrl
                         * @memberof edu.cust.secad.chat.proto.ChatMessageProtocolForm
                         * @static
                         * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                         * @returns {string} The default type url
                         */
                        ChatMessageProtocolForm.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                            if (typeUrlPrefix === undefined) {
                                typeUrlPrefix = "type.googleapis.com";
                            }
                            return typeUrlPrefix + "/edu.cust.secad.chat.proto.ChatMessageProtocolForm";
                        };

                        return ChatMessageProtocolForm;
                    })();

                    return proto;
                })();

                return chat;
            })();

            return secad;
        })();

        return cust;
    })();

    return edu;
})();

// 直接导出ChatMessageProtocolForm以便更方便地使用
export const ChatMessageProtocolForm = edu.cust.secad.chat.proto.ChatMessageProtocolForm;

export { $root as default };
